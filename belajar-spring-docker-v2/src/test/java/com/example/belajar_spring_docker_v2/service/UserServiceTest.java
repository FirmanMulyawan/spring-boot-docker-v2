package com.example.belajar_spring_docker_v2.service;

import com.example.belajar_spring_docker_v2.dto.UserRequestDTO;
import com.example.belajar_spring_docker_v2.dto.UserResponseDTO;
import com.example.belajar_spring_docker_v2.entity.User;
import com.example.belajar_spring_docker_v2.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Aktifkan fitur Mockito di class test ini
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // Buat boneka palsu UserRepository (tidak konek ke database beneran)
    @Mock
    private UserRepository userRepository;

    // Masukkan boneka ke dalam UserService yang ingin kita test
    @InjectMocks
    private UserService userService;

    // Data dummy yang dipakai berulang kali di semua test
    private User userDummy;

    @BeforeEach
    void setUp() {
        // Ini dijalankan sebelum SETIAP method @Test
        userDummy = new User();
        userDummy.setId(1L);
        userDummy.setName("Firman");
        userDummy.setEmail("firman@gmail.com");
    }

    // =========================================================
    // TEST: getAllUsers()
    // =========================================================

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        // ARRANGE: Ajarkan boneka → kalau findAll() dipanggil, kembalikan list berisi userDummy
        when(userRepository.findAll()).thenReturn(List.of(userDummy));

        // ACT: Jalankan method yang ingin ditest
        List<UserResponseDTO> result = userService.getAllUsers();

        // ASSERT: Periksa hasilnya
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Firman");
        assertThat(result.get(0).email()).isEqualTo("firman@gmail.com");

        // Verifikasi: pastikan findAll() benar-benar dipanggil 1 kali
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsers_WhenEmpty_ShouldReturnEmptyList() {
        // ARRANGE: Ajarkan boneka → kembalikan list kosong
        when(userRepository.findAll()).thenReturn(List.of());

        // ACT
        List<UserResponseDTO> result = userService.getAllUsers();

        // ASSERT
        assertThat(result).isEmpty();
    }

    // =========================================================
    // TEST: getUserById()
    // =========================================================

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // ARRANGE: Ajarkan boneka → kalau findById(1L) dipanggil, kembalikan userDummy
        when(userRepository.findById(1L)).thenReturn(Optional.of(userDummy));

        // ACT
        Optional<UserResponseDTO> result = userService.getUserById(1L);

        // ASSERT
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().name()).isEqualTo("Firman");
    }

    @Test
    void getUserById_WhenUserNotFound_ShouldReturnEmpty() {
        // ARRANGE: Ajarkan boneka → user dengan ID 99 tidak ada
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT
        Optional<UserResponseDTO> result = userService.getUserById(99L);

        // ASSERT
        assertThat(result).isEmpty();
    }

    // =========================================================
    // TEST: createUser()
    // =========================================================

    @Test
    void createUser_ShouldSaveAndReturnUser() {
        // ARRANGE
        UserRequestDTO requestDTO = new UserRequestDTO("Budi", "budi@gmail.com");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setName("Budi");
        savedUser.setEmail("budi@gmail.com");

        // Ajarkan boneka → kalau save() dipanggil dengan objek apapun, kembalikan savedUser
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // ACT
        UserResponseDTO result = userService.createUser(requestDTO);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Budi");
        assertThat(result.email()).isEqualTo("budi@gmail.com");

        // Verifikasi: save() dipanggil tepat 1 kali
        verify(userRepository, times(1)).save(any(User.class));
    }

    // =========================================================
    // TEST: updateUser()
    // =========================================================

    @Test
    void updateUser_WhenUserExists_ShouldUpdateAndReturn() {
        // ARRANGE
        UserRequestDTO requestDTO = new UserRequestDTO("Firman Updated", "updated@gmail.com");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("Firman Updated");
        updatedUser.setEmail("updated@gmail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userDummy));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // ACT
        Optional<UserResponseDTO> result = userService.updateUser(1L, requestDTO);

        // ASSERT
        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("Firman Updated");
        assertThat(result.get().email()).isEqualTo("updated@gmail.com");
    }

    @Test
    void updateUser_WhenUserNotFound_ShouldReturnEmpty() {
        // ARRANGE
        UserRequestDTO requestDTO = new UserRequestDTO("Siapa", "siapa@gmail.com");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT
        Optional<UserResponseDTO> result = userService.updateUser(99L, requestDTO);

        // ASSERT
        assertThat(result).isEmpty();

        // Verifikasi: save() TIDAK BOLEH dipanggil karena user tidak ada
        verify(userRepository, never()).save(any(User.class));
    }

    // =========================================================
    // TEST: deleteUser()
    // =========================================================

    @Test
    void deleteUser_WhenUserExists_ShouldReturnTrue() {
        // ARRANGE: User dengan ID 1 ada
        when(userRepository.existsById(1L)).thenReturn(true);

        // ACT
        boolean result = userService.deleteUser(1L);

        // ASSERT
        assertThat(result).isTrue();

        // Verifikasi: deleteById() dipanggil 1 kali dengan ID yang benar
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_WhenUserNotFound_ShouldReturnFalse() {
        // ARRANGE: User dengan ID 99 tidak ada
        when(userRepository.existsById(99L)).thenReturn(false);

        // ACT
        boolean result = userService.deleteUser(99L);

        // ASSERT
        assertThat(result).isFalse();

        // Verifikasi: deleteById() TIDAK BOLEH dipanggil
        verify(userRepository, never()).deleteById(anyLong());
    }
}
