package com.maximovich.planner.task.diagnostics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maximovich.planner.dtos.TransactionDiagnosticsResponse;
import com.maximovich.planner.repositories.ProjectRepository;
import com.maximovich.planner.repositories.TaskCommentRepository;
import com.maximovich.planner.repositories.TaskRepository;
import com.maximovich.planner.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
class TaskDiagnosticsIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskCommentRepository taskCommentRepository;

    @Test
    void shouldPersistPartialDataWithoutSharedTransaction() throws Exception {
        TransactionDiagnosticsResponse response = call("/api/tasks/diagnostics/transactions/without-transaction");

        assertThat(response.scenario()).isEqualTo("WITHOUT_TRANSACTION");
        assertThat(response.failed()).isTrue();
        assertThat(response.rollbackApplied()).isFalse();
        assertThat(response.persistedUsers()).isEqualTo(1);
        assertThat(response.persistedProjects()).isEqualTo(1);
        assertThat(response.persistedTasks()).isEqualTo(1);
        assertThat(response.persistedComments()).isZero();
        assertThat(userRepository.countByEmailContainingIgnoreCase(response.marker())).isEqualTo(1);
        assertThat(projectRepository.countByNameContainingIgnoreCase(response.marker())).isEqualTo(1);
        assertThat(taskRepository.countByTitleContainingIgnoreCase(response.marker())).isEqualTo(1);
        assertThat(taskCommentRepository.countByContentContainingIgnoreCase(response.marker())).isZero();
    }

    @Test
    void shouldRollbackAllDataWithinSharedTransaction() throws Exception {
        TransactionDiagnosticsResponse response = call("/api/tasks/diagnostics/transactions/with-transaction");

        assertThat(response.scenario()).isEqualTo("WITH_TRANSACTION");
        assertThat(response.failed()).isTrue();
        assertThat(response.rollbackApplied()).isTrue();
        assertThat(response.persistedUsers()).isZero();
        assertThat(response.persistedProjects()).isZero();
        assertThat(response.persistedTasks()).isZero();
        assertThat(response.persistedComments()).isZero();
        assertThat(userRepository.countByEmailContainingIgnoreCase(response.marker())).isZero();
        assertThat(projectRepository.countByNameContainingIgnoreCase(response.marker())).isZero();
        assertThat(taskRepository.countByTitleContainingIgnoreCase(response.marker())).isZero();
        assertThat(taskCommentRepository.countByContentContainingIgnoreCase(response.marker())).isZero();
    }

    private TransactionDiagnosticsResponse call(String path) throws Exception {
        MvcResult result = mockMvc.perform(post(path))
            .andExpect(status().isOk())
            .andReturn();

        return objectMapper.readValue(
            result.getResponse().getContentAsString(),
            TransactionDiagnosticsResponse.class
        );
    }
}
