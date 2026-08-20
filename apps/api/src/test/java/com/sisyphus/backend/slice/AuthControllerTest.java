package com.sisyphus.backend.slice;

import com.sisyphus.backend.auth.controller.AuthController;
import com.sisyphus.backend.auth.service.AuthService;
import com.sisyphus.backend.auth.service.EmailAuthService;
import com.sisyphus.backend.global.exception.UnauthorizedException;
import com.sisyphus.backend.global.props.AppProps;
import com.sisyphus.backend.global.props.FileProps;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        properties = {
                "app.hosts.app=http://localhost:3000",
                "app.hosts.api=http://localhost:8080",
                "app.hosts.img=http://localhost:8080",
                "app.hosts.chrome-extension=chrome-extension://test",
                "app.image.public-base=http://localhost:8080",
                "app.cors.allowed-origins=http://localhost:3000",
                "app.upload.allowed-extensions=png,jpg,jpeg",
                "file.upload-dir=/tmp/uploads",
                "file.access-url-prefix=/uploads/images/",
                "file.static-enabled=false"
        }
)
@ActiveProfiles("test")
@Import({
        com.sisyphus.backend.support.TestSecurityConfig.class,
        AuthControllerTest.TestPropsConfig.class
})
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    EmailAuthService emailAuthService;

    @Test
    void checkReturnsDuplicateFlag() throws Exception {
        when(authService.check(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/auth/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void validationErrorsUseSharedContract() throws Exception {
        mockMvc.perform(post("/api/auth/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/auth/check"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
    }

    @Test
    void refreshBootstrapsSessionFromValidCookie() throws Exception {
        when(authService.refreshAccessToken("valid-refresh")).thenReturn("new-access");

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "valid-refresh")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"));
    }

    @Test
    void refreshWithoutCookieFailsSafely() throws Exception {
        when(authService.refreshAccessToken(null))
                .thenThrow(new UnauthorizedException("Refresh token invalid or expired"));

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void refreshWithInvalidCookieFailsSafely() throws Exception {
        when(authService.refreshAccessToken("invalid-refresh"))
                .thenThrow(new UnauthorizedException("Refresh token invalid or expired"));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "invalid-refresh")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutInvalidatesRefreshStateAndExpiresCookie() throws Exception {
        when(authService.logout("valid-refresh"))
                .thenReturn(ResponseCookie.from("refreshToken", "")
                        .path("/")
                        .maxAge(0)
                        .build());

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "valid-refresh")))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));

        verify(authService).logout("valid-refresh");
    }

    @TestConfiguration
    @EnableConfigurationProperties({AppProps.class, FileProps.class})
    static class TestPropsConfig {
    }
}
