package com.fleetflow.ai.knowledge.service;

import com.fleetflow.ai.knowledge.dto.*;
import com.fleetflow.ai.knowledge.model.*;
import com.fleetflow.ai.knowledge.repository.*;
import com.fleetflow.ai.knowledge.security.KnowledgePrincipalService.Principal;
import com.fleetflow.ai.knowledge.storage.KnowledgeFileStorage;
import com.fleetflow.common.exception.BadRequestException;
import com.fleetflow.common.exception.ConflictException;
import com.fleetflow.common.exception.ResourceNotFoundException;
import com.fleetflow.user.enums.RoleName;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import java.time.Instant;
import java.util.*;

@Service
@ConditionalOnProperty(prefix = "fleetflow.knowledge", name = "enabled", havingValue = "true")
public class KnowledgeDocumentService {
    private final KnowledgeDocumentRepository documents;
    private final KnowledgeJobRepository jobs;
    private final KnowledgeFileStorage files;
    private final JdbcTemplate jdbc;

    public KnowledgeDocumentService(KnowledgeDocumentRepository documents, KnowledgeJobRepository jobs,
                                    KnowledgeFileStorage files, JdbcTemplate jdbc) {
        this.documents = documents; this.jobs = jobs; this.files = files; this.jdbc = jdbc;
    }

    @Transactional
    public KnowledgeDocumentResponse upload(MultipartFile file, DocumentType type, String version,
                                            List<String> accessRoles, UUID replacesId, Principal user) {
        if (type == null || version == null || !version.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,49}")) {
            throw new BadRequestException("Valid document type and version are required");
        }
        List<String> roles = validateRoles(accessRoles);
        if (replacesId != null) {
            KnowledgeDocument old = owned(replacesId, user);
            if (old.getStatus() != DocumentStatus.INDEXED || old.getDocumentType() != type) {
                throw new ConflictException("Replacement must target an indexed document of the same type");
            }
        }
        UUID id = UUID.randomUUID();
        var stored = files.store(file, user.organizationId(), id);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) files.delete(stored.storageKey());
            }
        });
        if (documents.existsByOrganizationIdAndChecksumAndStatusNot(user.organizationId(), stored.checksum(), DocumentStatus.DELETED)) {
            throw new ConflictException("Document content is already uploaded in this organization");
        }
        KnowledgeDocument d = new KnowledgeDocument();
        d.setId(id); d.setOrganizationId(user.organizationId()); d.setUploadedBy(user.userId());
        d.setFilename(stored.filename()); d.setStorageKey(stored.storageKey()); d.setDocumentType(type);
        d.setVersion(version); d.setStatus(DocumentStatus.UPLOADED); d.setChecksum(stored.checksum());
        d.setAccessRoles(roles); d.setReplacesDocumentId(replacesId);
        try { documents.saveAndFlush(d); }
        catch (DataIntegrityViolationException ex) { throw new ConflictException("Document content is already uploaded in this organization"); }
        queue(d, KnowledgeJob.Kind.INDEX);
        return KnowledgeDocumentResponse.from(d);
    }

    @Transactional(readOnly = true)
    public Page<KnowledgeDocumentResponse> list(Principal user, Pageable page) {
        return documents.findByOrganizationIdAndStatusNot(user.organizationId(), DocumentStatus.DELETED, page)
                .map(KnowledgeDocumentResponse::from);
    }

    @Transactional(readOnly = true)
    public KnowledgeDocumentResponse get(UUID id, Principal user) { return KnowledgeDocumentResponse.from(owned(id, user)); }

    @Transactional(readOnly = true)
    public KnowledgeStatusResponse status(UUID id, Principal user) {
        KnowledgeDocument d = owned(id, user);
        KnowledgeJob job = jobs.findByDocumentIdAndOrganizationId(id, user.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge job not found"));
        return new KnowledgeStatusResponse(id, d.getStatus(), job.getState(), job.getAttempts(),
                job.getErrorMessage() == null ? d.getErrorMessage() : job.getErrorMessage());
    }

    @Transactional
    public KnowledgeDocumentResponse updateAccess(UUID id, AccessUpdateRequest request, Principal user) {
        KnowledgeDocument d = locked(id, user);
        if (d.getStatus() == DocumentStatus.DELETING || d.getStatus() == DocumentStatus.DELETED) {
            throw new ConflictException("Document is being deleted");
        }
        List<String> roles = validateRoles(request.accessRoles());
        d.setAccessRoles(roles);
        String json = "[" + String.join(",", roles.stream().map(r -> "\"" + r + "\"").toList()) + "]";
        jdbc.update("UPDATE vector_store SET metadata = jsonb_set(metadata::jsonb, '{accessRoles}', ?::jsonb)::json WHERE metadata->>'documentId' = ? AND metadata->>'organizationId' = ?",
                json, id.toString(), user.organizationId().toString());
        return KnowledgeDocumentResponse.from(d);
    }

    @Transactional
    public KnowledgeStatusResponse reindex(UUID id, Principal user) {
        KnowledgeDocument d = locked(id, user);
        if (d.getStatus() != DocumentStatus.INDEXED) throw new ConflictException("Only indexed documents can be reindexed");
        KnowledgeJob job = jobs.findByDocumentIdAndOrganizationId(id, user.organizationId()).orElseThrow();
        if (job.getState() == KnowledgeJob.State.RUNNING || job.getState() == KnowledgeJob.State.QUEUED) {
            throw new ConflictException("Document processing is already queued");
        }
        reset(job, KnowledgeJob.Kind.INDEX);
        return new KnowledgeStatusResponse(id, d.getStatus(), job.getState(), job.getAttempts(), null);
    }

    @Transactional
    public KnowledgeStatusResponse retry(UUID id, Principal user) {
        KnowledgeDocument d = locked(id, user);
        KnowledgeJob job = jobs.findByDocumentIdAndOrganizationId(id, user.organizationId()).orElseThrow();
        if (job.getState() != KnowledgeJob.State.FAILED) throw new ConflictException("No failed job to retry");
        if (job.getKind() == KnowledgeJob.Kind.INDEX && d.getStatus() == DocumentStatus.FAILED) d.setStatus(DocumentStatus.UPLOADED);
        reset(job, job.getKind());
        return new KnowledgeStatusResponse(id, d.getStatus(), job.getState(), job.getAttempts(), null);
    }

    @Transactional
    public KnowledgeStatusResponse delete(UUID id, Principal user) {
        KnowledgeDocument d = locked(id, user);
        if (d.getStatus() == DocumentStatus.DELETED) throw new ResourceNotFoundException("Knowledge document not found");
        KnowledgeJob job = jobs.findByDocumentIdAndOrganizationId(id, user.organizationId()).orElseThrow();
        if (job.getState() == KnowledgeJob.State.RUNNING) throw new ConflictException("Document processing is in progress");
        d.setStatus(DocumentStatus.DELETING);
        reset(job, KnowledgeJob.Kind.DELETE);
        return new KnowledgeStatusResponse(id, d.getStatus(), job.getState(), job.getAttempts(), null);
    }

    private void queue(KnowledgeDocument d, KnowledgeJob.Kind kind) {
        KnowledgeJob job = new KnowledgeJob();
        job.setId(UUID.randomUUID()); job.setDocumentId(d.getId()); job.setOrganizationId(d.getOrganizationId());
        job.setKind(kind); job.setState(KnowledgeJob.State.QUEUED); job.setGenerationId(UUID.randomUUID());
        job.setRunAfter(Instant.now()); jobs.save(job);
    }

    private void reset(KnowledgeJob job, KnowledgeJob.Kind kind) {
        job.setKind(kind); job.setState(KnowledgeJob.State.QUEUED); job.setGenerationId(UUID.randomUUID());
        job.setAttempts(0); job.setRunAfter(Instant.now()); job.setLeaseUntil(null); job.setErrorMessage(null);
    }

    private KnowledgeDocument owned(UUID id, Principal user) {
        return documents.findByIdAndOrganizationId(id, user.organizationId())
                .filter(d -> d.getStatus() != DocumentStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge document not found"));
    }
    private KnowledgeDocument locked(UUID id, Principal user) {
        return documents.findLocked(id, user.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge document not found"));
    }

    private List<String> validateRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) throw new BadRequestException("At least one access role is required");
        if (roles.stream().anyMatch(role -> role == null || role.isBlank())) throw new BadRequestException("Access roles cannot be blank");
        List<String> clean = roles.stream().map(String::trim).distinct().sorted().toList();
        for (String role : clean) {
            try { RoleName.valueOf(role); }
            catch (IllegalArgumentException ex) { throw new BadRequestException("Unknown access role: " + role); }
        }
        return clean;
    }
}
