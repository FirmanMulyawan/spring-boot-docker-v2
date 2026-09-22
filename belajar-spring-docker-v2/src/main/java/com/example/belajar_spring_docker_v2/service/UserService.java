package com.example.belajar_spring_docker_v2.service;

import com.example.belajar_spring_docker_v2.dto.UserRequestDTO;
import com.example.belajar_spring_docker_v2.dto.UserResponseDTO;
import com.example.belajar_spring_docker_v2.entity.User;
import com.example.belajar_spring_docker_v2.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public UserResponseDTO createUser(UserRequestDTO requestDTO) {
        User user = new User();
        user.setName(requestDTO.name());
        user.setEmail(requestDTO.email());
        
        User savedUser = userRepository.save(user);
        return UserResponseDTO.fromEntity(savedUser);
    }

    public Optional<UserResponseDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponseDTO::fromEntity);
    }

    public Optional<UserResponseDTO> updateUser(Long id, UserRequestDTO requestDTO) {
        Optional<User> existingUserOpt = userRepository.findById(id);
        if (existingUserOpt.isEmpty()) {
            return Optional.empty();
        }

        User existingUser = existingUserOpt.get();
        existingUser.setName(requestDTO.name());
        existingUser.setEmail(requestDTO.email());

        User updatedUser = userRepository.save(existingUser);
        return Optional.of(UserResponseDTO.fromEntity(updatedUser));
    }

    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }
}
