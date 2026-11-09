package com.fleetflow.ai.knowledge.service;

import com.fleetflow.ai.knowledge.model.*;
import com.fleetflow.ai.knowledge.reader.KnowledgeDocumentReader;
import com.fleetflow.ai.knowledge.repository.KnowledgeDocumentRepository;
import com.fleetflow.ai.knowledge.repository.KnowledgeJobRepository;
import com.fleetflow.ai.knowledge.storage.KnowledgeFileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
@ConditionalOnProperty(prefix = "fleetflow.knowledge", name = "enabled", havingValue = "true")
public class KnowledgeIngestionWorker {
    private static final Logger log = LoggerFactory.getLogger(KnowledgeIngestionWorker.class);
    private final JdbcTemplate jdbc;
    private final KnowledgeDocumentRepository documents;
    private final KnowledgeJobRepository jobs;
    private final KnowledgeFileStorage files;
    private final KnowledgeDocumentReader reader;
    private final VectorStore vectors;
    private final TransactionTemplate transactions;
    private final int chunkSize;
    private final int maxAttempts;

    public KnowledgeIngestionWorker(JdbcTemplate jdbc, KnowledgeDocumentRepository documents,
                                    KnowledgeJobRepository jobs, KnowledgeFileStorage files,
                                    KnowledgeDocumentReader reader, VectorStore vectors,
                                    TransactionTemplate transactions,
                                    @Value("${fleetflow.knowledge.chunk-size:800}") int chunkSize,
                                    @Value("${fleetflow.knowledge.max-attempts:3}") int maxAttempts) {
        this.jdbc = jdbc; this.documents = documents; this.jobs = jobs; this.files = files;
        this.reader = reader; this.vectors = vectors; this.transactions = transactions;
        this.chunkSize = Math.max(100, Math.min(chunkSize, 1500));
        this.maxAttempts = Math.max(1, maxAttempts);
    }

    @Scheduled(fixedDelayString = "${fleetflow.knowledge.worker-delay-ms:2000}")
    public void poll() {
        List<JobClaim> claims = jdbc.query("""
                UPDATE knowledge_jobs SET state='RUNNING', attempts=attempts+1,
                    lease_until=NOW() + INTERVAL '30 minutes', updated_at=NOW()
                WHERE id = (SELECT id FROM knowledge_jobs
                    WHERE (state='QUEUED' AND run_after <= NOW())
                       OR (state='RUNNING' AND lease_until < NOW())
                    ORDER BY run_after, created_at FOR UPDATE SKIP LOCKED LIMIT 1)
                RETURNING id, document_id, organization_id, kind, generation_id, attempts
                """, (rs, row) -> new JobClaim(
                rs.getObject("id", UUID.class), rs.getObject("document_id", UUID.class),
                rs.getObject("organization_id", UUID.class), KnowledgeJob.Kind.valueOf(rs.getString("kind")),
                rs.getObject("generation_id", UUID.class), rs.getInt("attempts")));
        if (claims.isEmpty()) return;
        JobClaim claim = claims.getFirst();
        try {
            if (claim.kind() == KnowledgeJob.Kind.DELETE) delete(claim);
            else index(claim);
        } catch (Exception ex) {
            log.warn("Knowledge job {} failed: {}", claim.id(), ex.getClass().getSimpleName());
            if (claim.kind() == KnowledgeJob.Kind.INDEX) {
                try { vectors.delete(scopedGeneration(claim)); }
                catch (Exception cleanup) { log.warn("Knowledge vector cleanup pending for job {}", claim.id()); }
            }
            fail(claim);
        }
    }

    private void index(JobClaim claim) {
        KnowledgeDocument d = documents.findByIdAndOrganizationId(claim.documentId(), claim.organizationId())
                .orElseThrow(() -> new IllegalStateException("Document missing"));
        if (d.getStatus() == DocumentStatus.DELETED || d.getStatus() == DocumentStatus.DELETING) {
            throw new IllegalStateException("Document is deleting");
        }
        transactions.executeWithoutResult(tx -> {
            KnowledgeDocument current = documents.findById(claim.documentId()).orElseThrow();
            if (current.getActiveGeneration() == null) current.setStatus(DocumentStatus.PROCESSING);
            current.setErrorMessage(null);
        });
        // A previous attempt may have inserted some chunks before it failed.
        vectors.delete(scopedGeneration(claim));
        var pages = reader.read(files.resolve(d.getStorageKey()));
        TokenTextSplitter splitter = TokenTextSplitter.builder().withChunkSize(chunkSize)
                .withMinChunkSizeChars(100).withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(1500).withKeepSeparator(true).build();
        List<Document> chunks = new ArrayList<>();
        for (var page : pages) {
            Map<String, Object> source = new HashMap<>();
            source.put("documentId", d.getId().toString());
            source.put("organizationId", d.getOrganizationId().toString());
            source.put("generation", claim.generationId().toString());
            source.put("filename", d.getFilename());
            source.put("version", d.getVersion());
            source.put("documentType", d.getDocumentType().name());
            source.put("accessRoles", d.getAccessRoles());
            source.put("active", false);
            if (page.pageNumber() != null) source.put("pageNumber", page.pageNumber());
            for (Document part : splitter.apply(List.of(new Document(page.text(), source)))) {
                int position = chunks.size();
                String stableKey = d.getId() + ":" + claim.generationId() + ":" + position;
                String id = UUID.nameUUIDFromBytes(stableKey.getBytes(StandardCharsets.UTF_8)).toString();
                Map<String, Object> metadata = new HashMap<>(part.getMetadata());
                metadata.put("chunkIndex", position);
                chunks.add(new Document(id, part.getText(), metadata));
                if (chunks.size() > 1500) throw new IllegalArgumentException("Document produces too many chunks");
            }
        }
        if (chunks.isEmpty()) throw new IllegalArgumentException("Document has no indexable text");
        vectors.add(chunks);
        transactions.executeWithoutResult(tx -> activate(claim, chunks.size()));
    }

    private void activate(JobClaim claim, int chunkCount) {
        KnowledgeDocument d = documents.findByIdAndOrganizationId(claim.documentId(), claim.organizationId()).orElseThrow();
        KnowledgeJob job = jobs.findById(claim.id()).orElseThrow();
        if (job.getState() != KnowledgeJob.State.RUNNING || !claim.generationId().equals(job.getGenerationId())) {
            throw new IllegalStateException("Job was replaced during processing");
        }
        int activated = jdbc.update("""
                UPDATE vector_store SET metadata = jsonb_set(metadata::jsonb, '{active}', 'true'::jsonb)::json
                WHERE metadata->>'documentId' = ? AND metadata->>'organizationId' = ?
                  AND metadata->>'generation' = ?
                """, d.getId().toString(), d.getOrganizationId().toString(), claim.generationId().toString());
        if (activated != chunkCount) throw new IllegalStateException("Incomplete vector indexing");
        UUID previous = d.getActiveGeneration();
        d.setActiveGeneration(claim.generationId()); d.setChunkCount(chunkCount);
        d.setStatus(DocumentStatus.INDEXED); d.setIndexedAt(Instant.now()); d.setErrorMessage(null);
        job.setState(KnowledgeJob.State.DONE); job.setLeaseUntil(null); job.setErrorMessage(null);
        if (d.getReplacesDocumentId() != null) {
            int replaced = jdbc.update("UPDATE knowledge_documents SET status='SUPERSEDED', updated_at=NOW() WHERE id=? AND organization_id=? AND status='INDEXED'",
                    d.getReplacesDocumentId(), d.getOrganizationId());
            if (replaced != 1) throw new IllegalStateException("Replacement target is no longer active");
        }
        if (previous != null && !previous.equals(claim.generationId())) {
            vectors.delete(new FilterExpressionBuilder().and(
                    new FilterExpressionBuilder().eq("documentId", d.getId().toString()),
                    new FilterExpressionBuilder().eq("generation", previous.toString())).build());
        }
    }

    private void delete(JobClaim claim) {
        KnowledgeDocument d = documents.findByIdAndOrganizationId(claim.documentId(), claim.organizationId()).orElseThrow();
        transactions.executeWithoutResult(tx -> {
            vectors.delete(new FilterExpressionBuilder().and(
                    new FilterExpressionBuilder().eq("documentId", d.getId().toString()),
                    new FilterExpressionBuilder().eq("organizationId", d.getOrganizationId().toString())).build());
            KnowledgeDocument current = documents.findById(d.getId()).orElseThrow();
            KnowledgeJob job = jobs.findById(claim.id()).orElseThrow();
            current.setStatus(DocumentStatus.DELETED); current.setDeletedAt(Instant.now());
            current.setChunkCount(0); current.setActiveGeneration(null);
            job.setState(KnowledgeJob.State.DONE); job.setLeaseUntil(null);
        });
        try { files.delete(d.getStorageKey()); }
        catch (Exception ex) { log.warn("Knowledge file cleanup remains pending for document {}", d.getId()); }
    }

    private void fail(JobClaim claim) {
        transactions.executeWithoutResult(tx -> {
            KnowledgeJob job = jobs.findById(claim.id()).orElseThrow();
            KnowledgeDocument d = documents.findById(claim.documentId()).orElseThrow();
            boolean retry = claim.attempts() < maxAttempts;
            job.setState(retry ? KnowledgeJob.State.QUEUED : KnowledgeJob.State.FAILED);
            job.setLeaseUntil(null);
            job.setRunAfter(Instant.now().plusSeconds(Math.min(300, 5L * (1L << Math.min(claim.attempts(), 5)))));
            job.setErrorMessage("Document processing failed");
            d.setErrorMessage("Document processing failed");
            if (claim.kind() == KnowledgeJob.Kind.INDEX && d.getActiveGeneration() == null) {
                d.setStatus(retry ? DocumentStatus.UPLOADED : DocumentStatus.FAILED);
            }
        });
    }

    private org.springframework.ai.vectorstore.filter.Filter.Expression scopedGeneration(JobClaim claim) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        return b.and(b.eq("documentId", claim.documentId().toString()),
                b.and(b.eq("organizationId", claim.organizationId().toString()),
                        b.eq("generation", claim.generationId().toString()))).build();
    }

    @Scheduled(fixedDelayString = "${fleetflow.knowledge.cleanup-delay-ms:3600000}")
    public void cleanDeletedFiles() {
        jdbc.query("SELECT storage_key FROM knowledge_documents WHERE status='DELETED'", rs -> {
            try { files.delete(rs.getString(1)); }
            catch (Exception ex) { log.warn("Knowledge file cleanup remains pending"); }
        });
    }

    private record JobClaim(UUID id, UUID documentId, UUID organizationId,
                            KnowledgeJob.Kind kind, UUID generationId, int attempts) {}
}
