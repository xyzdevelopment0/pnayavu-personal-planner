package com.maximovich.planner.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maximovich.planner.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class ApiInfrastructureIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    void shouldReturnUnifiedValidationErrorForRequestBody() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": " ",
                      "email": "invalid-email"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.path").value("/api/users"))
            .andExpect(jsonPath("$.details[*].field", hasItem("name")))
            .andExpect(jsonPath("$.details[*].field", hasItem("email")));
    }

    @Test
    void shouldReturnUnifiedValidationErrorForRequestParam() throws Exception {
        mockMvc.perform(get("/api/tasks/search/jpql").param("ownerEmail", "invalid-email"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.path").value("/api/tasks/search/jpql"))
            .andExpect(jsonPath("$.details[*].field", hasItem("ownerEmail")));
    }

    @Test
    void shouldReturnUnifiedNotFoundError() throws Exception {
        mockMvc.perform(get("/api/users/999999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
            .andExpect(jsonPath("$.path").value("/api/users/999999"))
            .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    void shouldExposeSwaggerDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.info.title").value("Personal Planner Swagger"))
            .andExpect(jsonPath("$.paths['/api/users'].post.summary").value("Create user"))
            .andExpect(jsonPath("$.components.schemas.UserRequest.description")
                .value("Request for creating or updating a user"));
    }

    @Test
    void shouldLogServiceExecutionTime(CapturedOutput output) {
        userService.findAll();

        assertThat(output.getOut()).contains("UserService.findAll completed in");
    }
}
