package com.example.belajar_spring_docker_v2.controller;

import com.example.belajar_spring_docker_v2.dto.UserResponseDTO;
import com.example.belajar_spring_docker_v2.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Hanya load layer Controller saja (lebih ringan dari @SpringBootTest)
@WebMvcTest(UserController.class)
// Import konfigurasi khusus test yang menyediakan CacheManager NoOp (tanpa Redis)
@Import(UserControllerTest.TestCacheConfig.class)
class UserControllerTest {

    /**
     * Konfigurasi khusus untuk test ini.
     * Menyediakan CacheManager palsu (NoOp) agar @Cacheable/@CacheEvict
     * di UserService tidak error karena tidak ada Redis saat test.
     *
     * NoOp = "No Operation" → cache tidak melakukan apa-apa (tidak cache, tidak error)
     */
    @TestConfiguration
    static class TestCacheConfig {
        @Bean
        public CacheManager cacheManager() {
            return new NoOpCacheManager();
        }
    }

    // Robot pengirim request HTTP palsu
    @Autowired
    private MockMvc mockMvc;

    // Buat boneka Spring Bean untuk UserService
    @MockitoBean
    private UserService userService;

    // =========================================================
    // TEST: GET /users
    // =========================================================

    @Test
    void getUsers_ShouldReturn200AndListOfUsers() throws Exception {
        // ARRANGE: Siapkan data dummy & ajarkan boneka
        UserResponseDTO dto = new UserResponseDTO(1L, "Firman", "firman@gmail.com");
        when(userService.getAllUsers()).thenReturn(List.of(dto));

        // ACT & ASSERT: Kirim request dan periksa response sekaligus
        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())                            // HTTP 200
                .andExpect(jsonPath("$").isArray())                    // Response berupa array
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Firman"))
                .andExpect(jsonPath("$[0].email").value("firman@gmail.com"));
    }

    @Test
    void getUsers_WhenEmpty_ShouldReturn200AndEmptyArray() throws Exception {
        // ARRANGE: Tidak ada user sama sekali
        when(userService.getAllUsers()).thenReturn(List.of());

        // ACT & ASSERT
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // =========================================================
    // TEST: GET /users/{id}
    // =========================================================

    @Test
    void getUserById_WhenFound_ShouldReturn200() throws Exception {
        // ARRANGE
        UserResponseDTO dto = new UserResponseDTO(1L, "Firman", "firman@gmail.com");
        when(userService.getUserById(1L)).thenReturn(Optional.of(dto));

        // ACT & ASSERT
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Firman"))
                .andExpect(jsonPath("$.email").value("firman@gmail.com"));
    }

    @Test
    void getUserById_WhenNotFound_ShouldReturn404() throws Exception {
        // ARRANGE: User ID 99 tidak ada
        when(userService.getUserById(99L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound()); // HTTP 404
    }

    // =========================================================
    // TEST: POST /users
    // =========================================================

    @Test
    void createUser_ShouldReturn201AndCreatedUser() throws Exception {
        // ARRANGE
        UserResponseDTO responseDTO = new UserResponseDTO(1L, "Budi", "budi@gmail.com");
        when(userService.createUser(any())).thenReturn(responseDTO);

        // ACT & ASSERT
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Budi",
                                  "email": "budi@gmail.com"
                                }
                                """))
                .andExpect(status().isCreated())            // HTTP 201
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Budi"))
                .andExpect(jsonPath("$.email").value("budi@gmail.com"));
    }

    @Test
    void createUser_WhenNameBlank_ShouldReturn400() throws Exception {
        // ARRANGE: Kirim request dengan name kosong (melanggar @NotBlank)
        // ACT & ASSERT
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "email": "budi@gmail.com"
                                }
                                """))
                .andExpect(status().isBadRequest()); // HTTP 400 dari @Valid
    }

    @Test
    void createUser_WhenEmailInvalid_ShouldReturn400() throws Exception {
        // ARRANGE: Kirim request dengan email tidak valid
        // ACT & ASSERT
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Budi",
                                  "email": "ini-bukan-email"
                                }
                                """))
                .andExpect(status().isBadRequest()); // HTTP 400
    }

    // =========================================================
    // TEST: PUT /users/{id}
    // =========================================================

    @Test
    void updateUser_WhenUserExists_ShouldReturn200() throws Exception {
        // ARRANGE
        UserResponseDTO updatedDTO = new UserResponseDTO(1L, "Firman Updated", "updated@gmail.com");
        when(userService.updateUser(eq(1L), any())).thenReturn(Optional.of(updatedDTO));

        // ACT & ASSERT
        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Firman Updated",
                                  "email": "updated@gmail.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Firman Updated"))
                .andExpect(jsonPath("$.email").value("updated@gmail.com"));
    }

    @Test
    void updateUser_WhenUserNotFound_ShouldReturn404() throws Exception {
        // ARRANGE: User ID 99 tidak ada
        when(userService.updateUser(eq(99L), any())).thenReturn(Optional.empty());

        // ACT & ASSERT
        mockMvc.perform(put("/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Siapa",
                                  "email": "siapa@gmail.com"
                                }
                                """))
                .andExpect(status().isNotFound()); // HTTP 404
    }

    // =========================================================
    // TEST: DELETE /users/{id}
    // =========================================================

    @Test
    void deleteUser_WhenUserExists_ShouldReturn204() throws Exception {
        // ARRANGE: User ID 1 ada dan berhasil dihapus
        when(userService.deleteUser(1L)).thenReturn(true);

        // ACT & ASSERT
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent()); // HTTP 204
    }

    @Test
    void deleteUser_WhenUserNotFound_ShouldReturn404() throws Exception {
        // ARRANGE: User ID 99 tidak ada
        when(userService.deleteUser(99L)).thenReturn(false);

        // ACT & ASSERT
        mockMvc.perform(delete("/users/99"))
                .andExpect(status().isNotFound()); // HTTP 404
    }
}
