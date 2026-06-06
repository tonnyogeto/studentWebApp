package com.example.StudentWebApp.classstream.repository;

import java.util.Optional;

import com.example.StudentWebApp.classstream.entity.ClassStream;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassStreamRepository extends JpaRepository<ClassStream, Long> {

    Optional<ClassStream> findByStreamCode(String streamCode);

    boolean existsByStreamCodeAndIdNot(String streamCode, Long id);
}
