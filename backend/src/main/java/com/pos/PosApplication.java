package com.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// NOTE: @EnableJpaAuditing intentionally lives on JpaAuditingConfig, not here - keeping it
// off the main @SpringBootApplication class means @WebMvcTest slices (which don't load a
// JPA EntityManagerFactory) don't try to build the JPA auditing infrastructure and fail
// with "JPA metamodel must not be empty".
@SpringBootApplication
public class PosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PosApplication.class, args);
    }
}
