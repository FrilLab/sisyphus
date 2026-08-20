package com.sisyphus.backend.slice;

import com.sisyphus.backend.category.controller.CategoryController;
import com.sisyphus.backend.category.dto.CategoryResponse;
import com.sisyphus.backend.category.service.CategoryService;
import com.sisyphus.backend.global.props.AppProps;
import com.sisyphus.backend.global.props.FileProps;
import com.sisyphus.backend.image.controller.ImageController;
import com.sisyphus.backend.image.dto.ImageUploadResponse;
import com.sisyphus.backend.image.service.ImageService;
import com.sisyphus.backend.require.controller.RequireController;
import com.sisyphus.backend.require.exception.RequireNotFoundException;
import com.sisyphus.backend.require.service.RequireService;
import com.sisyphus.backend.search.controller.SearchController;
import com.sisyphus.backend.search.service.SearchService;
import com.sisyphus.backend.security.principal.UserPrincipal;
import com.sisyphus.backend.tag.controller.TagController;
import com.sisyphus.backend.tag.dto.TagResponse;
import com.sisyphus.backend.tag.service.TagService;
import com.sisyphus.backend.user.controller.UserController;
import com.sisyphus.backend.user.dto.UserResponse;
import com.sisyphus.backend.user.entity.User;
import com.sisyphus.backend.user.service.AccountService;
import com.sisyphus.backend.user.service.UserService;
import com.sisyphus.backend.user.util.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {
                CategoryController.class,
                TagController.class,
                RequireController.class,
                SearchController.class,
                UserController.class,
                ImageController.class
        },
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
        PublicApiControllerTest.TestPropsConfig.class
})
class PublicApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CategoryService categoryService;

    @MockitoBean
    TagService tagService;

    @MockitoBean
    RequireService requireService;

    @MockitoBean
    SearchService searchService;

    @MockitoBean
    UserService userService;

    @MockitoBean
    AccountService accountService;

    @MockitoBean
    ImageService imageService;

    @Test
    void categoryRouteSerializesServiceResponse() throws Exception {
        when(categoryService.getAllCategories(7L))
                .thenReturn(List.of(new CategoryResponse(3L, "Work", "#1186ce")));

        mockMvc.perform(get("/api/category/all").with(user(principal())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].title").value("Work"));
    }

    @Test
    void tagUpdateBindsRequestAndSerializesResponse() throws Exception {
        when(tagService.update(4L, "spring", 7L))
                .thenReturn(new TagResponse(4L, "spring"));

        mockMvc.perform(put("/api/tag/update")
                        .with(user(principal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 4,
                                  "name": "spring"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.name").value("spring"));
    }

    @Test
    void requireNotFoundUsesSharedErrorContract() throws Exception {
        when(requireService.getRequireById(7L, 99L))
                .thenThrow(new RequireNotFoundException());

        mockMvc.perform(get("/api/require/99").with(user(principal())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void missingSearchQueryIsRejectedBeforeCallingService() throws Exception {
        mockMvc.perform(get("/api/search").with(user(principal())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("q"));
    }

    @Test
    void searchRoutePassesPaginationToTheService() throws Exception {
        when(searchService.search(eq("oauth"), eq(7L), any(Pageable.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/search")
                        .with(user(principal()))
                        .param("q", "oauth")
                        .param("page", "1")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void userRouteSerializesTheAuthenticatedUsersProfile() throws Exception {
        when(userService.getUserResponse(7L))
                .thenReturn(new UserResponse(new User("user@example.com", "Tester", Role.USER)));

        mockMvc.perform(post("/api/user/read").with(user(principal())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void imageUploadReturnsCreatedMetadata() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{1, 2, 3}
        );
        when(imageService.store(any()))
                .thenReturn(new ImageUploadResponse(12L, "/uploads/sample.png", "sample.png", "png", 3L));

        mockMvc.perform(multipart("/api/image").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.url").value("/uploads/sample.png"));
    }

    @Test
    void unexpectedFailureUsesInternalErrorContractWithoutLeakingDetails() throws Exception {
        when(categoryService.getAllCategories(7L))
                .thenThrow(new IllegalStateException("database password=secret"));

        mockMvc.perform(get("/api/category/all").with(user(principal())))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다."));
    }

    private UserPrincipal principal() {
        return new UserPrincipal(
                7L,
                "user@example.com",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @TestConfiguration
    @EnableConfigurationProperties({AppProps.class, FileProps.class})
    static class TestPropsConfig {
    }
}
