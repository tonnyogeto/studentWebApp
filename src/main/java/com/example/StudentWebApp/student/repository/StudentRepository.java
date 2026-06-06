package com.example.StudentWebApp.student.repository;

import java.util.List;
import java.util.Optional;

import com.example.StudentWebApp.student.entity.Student;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByAdmissionNumber(String admissionNumber);

    boolean existsByAdmissionNumberAndIdNot(String admissionNumber, Long id);

    List<Student> findByClassStreamIdOrderByLastNameAscFirstNameAsc(Long classStreamId);
}
