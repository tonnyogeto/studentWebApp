package com.example.StudentWebApp.subject.repository;

import java.util.Optional;

import com.example.StudentWebApp.subject.entity.Subject;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findBySubjectCode(String subjectCode);

    boolean existsBySubjectCodeAndIdNot(String subjectCode, Long id);

    boolean existsBySubjectNameAndIdNot(String subjectName, Long id);
}
