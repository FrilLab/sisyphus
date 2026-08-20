package com.sisyphus.backend.integration;

import com.sisyphus.backend.BackendApplication;
import com.sisyphus.backend.security.principal.UserPrincipal;
import com.sisyphus.backend.support.MockBeans;
import com.sisyphus.backend.user.entity.User;
import com.sisyphus.backend.user.repository.UserRepository;
import com.sisyphus.backend.user.util.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(MockBeans.class)
@Transactional
class NoteLifecycleIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Test
    void createsRetrievesAndUpdatesANoteThroughTheApi() throws Exception {
        UserPrincipal principal = persistPrincipal("lifecycle@example.com");

        String id = mockMvc.perform(post("/api/note/create")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "First title",
                                  "subTitle": "Optional subtitle",
                                  "description": "Initial description"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(get("/api/note/read/{id}", id).with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("First title"))
                .andExpect(jsonPath("$.subTitle").value("Optional subtitle"));

        mockMvc.perform(put("/api/note/update/{id}", id)
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated title",
                                  "subTitle": null,
                                  "description": "Persisted update"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.subTitle").doesNotExist())
                .andExpect(jsonPath("$.description").value("Persisted update"));

        mockMvc.perform(get("/api/note/read/{id}", id).with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.description").value("Persisted update"));
    }

    @Test
    void unknownNoteUsesTheSharedNotFoundContract() throws Exception {
        UserPrincipal principal = persistPrincipal("missing@example.com");

        mockMvc.perform(get("/api/note/read/{id}", 999_999L).with(user(principal)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/note/read/999999"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void validationFailureUsesTheSharedErrorContract() throws Exception {
        mockMvc.perform(post("/api/auth/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
    }

    private UserPrincipal persistPrincipal(String email) {
        User user = userRepository.saveAndFlush(new User(email, "Tester", Role.USER));
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
