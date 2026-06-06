package com.example.StudentWebApp.assessment.repository;

import java.util.List;
import java.util.Optional;

import com.example.StudentWebApp.assessment.entity.Assessment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    Optional<Assessment> findByStudentIdAndSubjectIdAndTerm(Long studentId, Long subjectId, String term);

    boolean existsByStudentId(Long studentId);

    boolean existsBySubjectId(Long subjectId);

    List<Assessment> findByStudentIdAndTermOrderBySubjectSubjectNameAsc(Long studentId, String term);

    @Query("select a from Assessment a join fetch a.student s join fetch a.subject sub where s.classStream.id = :classStreamId and a.term = :term order by s.lastName asc, s.firstName asc, sub.subjectName asc")
    List<Assessment> findForClassStreamAndTerm(@Param("classStreamId") Long classStreamId, @Param("term") String term);
}
