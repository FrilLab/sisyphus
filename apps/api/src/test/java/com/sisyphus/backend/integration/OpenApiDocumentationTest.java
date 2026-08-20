package com.sisyphus.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sisyphus.backend.BackendApplication;
import com.sisyphus.backend.support.MockBeans;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(MockBeans.class)
class OpenApiDocumentationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void generatedDocumentDescribesMetadataSecurityRoutesSchemasAndErrors() throws Exception {
        String specification = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode document = objectMapper.readTree(specification);
        OpenAPI parsedSpecification = Json.mapper().readValue(specification, OpenAPI.class);

        assertThat(document.path("openapi").asText()).startsWith("3.");
        assertThat(parsedSpecification.getPaths()).isNotEmpty();
        assertThat(document.at("/info/title").asText()).isEqualTo("Sisyphus Academy API");
        assertThat(document.at("/info/version").asText()).isEqualTo("0.0.1");
        assertThat(document.at("/info/license/name").asText()).isEqualTo("Apache License 2.0");
        assertThat(document.at("/components/securitySchemes/bearerAuth/scheme").asText())
                .isEqualTo("bearer");

        assertThat(document.at("/paths/~1api~1auth~1login/post").isObject()).isTrue();
        assertThat(document.at("/paths/~1api~1note~1create/post").isObject()).isTrue();
        assertThat(document.at("/paths/~1api~1category~1update~1{id}/put/summary").asText())
                .isEqualTo("Update category");
        assertThat(document.at("/paths/~1api~1dev~1note~1seed").isMissingNode()).isTrue();

        JsonNode noteSecurity = document.at("/paths/~1api~1note~1create/post/security/0/bearerAuth");
        assertThat(noteSecurity.isArray()).isTrue();
        assertThat(document.at("/paths/~1api~1auth~1login/post/security").isMissingNode()).isTrue();

        assertThat(document.at("/components/schemas/CategoryRequest").isObject()).isTrue();
        assertThat(document.at("/components/schemas/TokenResponse").isObject()).isTrue();
        assertThat(document.at("/components/schemas/ErrorResponse/properties/fieldErrors/type").asText())
                .isEqualTo("array");
        assertThat(document.at("/components/responses/ValidationError").isObject()).isTrue();
        assertThat(document.at("/components/responses/InternalServerError").isObject()).isTrue();
        assertThat(document
                .at("/paths/~1api~1category~1update~1{id}/put/responses/404/content/application~1json/schema/$ref")
                .asText())
                .isEqualTo("#/components/schemas/ErrorResponse");
        assertAllOperationsGroupedAndDescribed(document.path("paths"));

        Path output = Path.of("build/generated/openapi/openapi.json");
        Files.createDirectories(output.getParent());
        Files.writeString(output, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(document));
    }

    @Test
    void swaggerUiIsAvailableInTheDevelopmentDefaultConfiguration() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    private void assertAllOperationsGroupedAndDescribed(JsonNode paths) {
        Set<String> methods = Set.of("get", "post", "put", "patch", "delete");
        paths.fields().forEachRemaining(pathEntry ->
                pathEntry.getValue().fields().forEachRemaining(operationEntry -> {
                    if (!methods.contains(operationEntry.getKey())) {
                        return;
                    }
                    JsonNode operation = operationEntry.getValue();
                    String location = operationEntry.getKey().toUpperCase() + " " + pathEntry.getKey();
                    assertThat(operation.path("summary").asText())
                            .as("%s has a summary", location)
                            .isNotBlank();
                    assertThat(operation.path("tags").isArray() && !operation.path("tags").isEmpty())
                            .as("%s belongs to a domain tag", location)
                            .isTrue();
                })
        );
    }
}
