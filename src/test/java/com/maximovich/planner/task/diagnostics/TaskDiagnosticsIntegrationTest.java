package com.maximovich.planner.task.diagnostics;

import static org.assertj.core.api.Assertions.assertThat;

import com.maximovich.planner.task.diagnostics.dto.TransactionDemoResponse;
import com.maximovich.planner.task.diagnostics.service.TaskDiagnosticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class TaskDiagnosticsIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TaskDiagnosticsService taskDiagnosticsService;

    @Test
    void shouldReduceQueryCountWithEntityGraph() {
        var response = taskDiagnosticsService.demonstrateNPlusOne();

        assertThat(response.projects()).isNotEmpty();
        assertThat(response.nPlusOneQueryCount()).isGreaterThan(response.optimizedQueryCount());
    }

    @Test
    void shouldShowPartialCommitWithoutTransactionAndRollbackWithTransaction() {
        TransactionDemoResponse withoutTransaction = taskDiagnosticsService.demonstrateWithoutTransaction();
        TransactionDemoResponse withTransaction = taskDiagnosticsService.demonstrateWithTransaction();

        assertThat(withoutTransaction.errorMessage()).isNotBlank();
        assertThat(withoutTransaction.after().users()).isGreaterThan(withoutTransaction.before().users());
        assertThat(withoutTransaction.after().projects()).isGreaterThan(withoutTransaction.before().projects());
        assertThat(withoutTransaction.after().tasks()).isGreaterThan(withoutTransaction.before().tasks());
        assertThat(withoutTransaction.after().comments()).isGreaterThan(withoutTransaction.before().comments());
        assertThat(withoutTransaction.after().tags()).isEqualTo(withoutTransaction.before().tags());

        assertThat(withTransaction.errorMessage()).isNotBlank();
        assertThat(withTransaction.after()).isEqualTo(withTransaction.before());
    }
}
