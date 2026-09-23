package com.example.belajar_spring_docker_v2.dto;

import java.io.Serializable;

import com.example.belajar_spring_docker_v2.entity.User;

public record UserResponseDTO(Long id, String name, String email) implements Serializable {
    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail());
    }
}
