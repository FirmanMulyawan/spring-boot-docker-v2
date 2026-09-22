package com.example.belajar_spring_docker_v2.repository;

import com.example.belajar_spring_docker_v2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}