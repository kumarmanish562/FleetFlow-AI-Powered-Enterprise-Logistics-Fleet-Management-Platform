package com.fleetflow.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ApplicationMetadataRepositoryTest {

    @Autowired
    private ApplicationMetadataRepository repository;

    @Test
    void shouldLoadMetadata() {

        var metadata = repository.findAll();

        assertThat(metadata)
                .isNotEmpty();

        assertThat(metadata.getFirst().getApplicationName())
                .isEqualTo("FleetFlow");
    }
}
