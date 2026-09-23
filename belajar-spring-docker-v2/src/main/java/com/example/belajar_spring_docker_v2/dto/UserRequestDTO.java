package com.example.belajar_spring_docker_v2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequestDTO(
        @NotBlank(message = "Name is mandatory")
        String name,
        
        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email format is invalid")
        String email
) {
}
