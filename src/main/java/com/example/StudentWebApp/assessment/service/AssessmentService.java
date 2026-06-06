package com.example.StudentWebApp.assessment.service;

import java.util.List;

import com.example.StudentWebApp.assessment.dto.AssessmentRequest;
import com.example.StudentWebApp.assessment.dto.AssessmentResponse;
import com.example.StudentWebApp.assessment.entity.Assessment;
import com.example.StudentWebApp.assessment.repository.AssessmentRepository;
import com.example.StudentWebApp.common.exception.BadRequestException;
import com.example.StudentWebApp.common.exception.DuplicateAssessmentException;
import com.example.StudentWebApp.common.exception.ResourceNotFoundException;
import com.example.StudentWebApp.student.entity.Student;
import com.example.StudentWebApp.student.repository.StudentRepository;
import com.example.StudentWebApp.subject.entity.Subject;
import com.example.StudentWebApp.subject.repository.SubjectRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    public AssessmentService(AssessmentRepository assessmentRepository, StudentRepository studentRepository,
            SubjectRepository subjectRepository) {
        this.assessmentRepository = assessmentRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
    }

    public AssessmentResponse record(AssessmentRequest request) {
        String term = normalizeTerm(request.term());
        if (assessmentRepository.findByStudentIdAndSubjectIdAndTerm(request.studentId(), request.subjectId(), term)
                .isPresent()) {
            throw new DuplicateAssessmentException("Assessment already exists for this student, subject and term");
        }
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        if (student.getClassStream() == null || student.getClassStream().getSubjects().stream()
                .noneMatch(assignedSubject -> assignedSubject.getId().equals(subject.getId()))) {
            throw new BadRequestException("Subject is not assigned to the student's class stream");
        }
        Assessment assessment = new Assessment();
        assessment.setStudent(student);
        assessment.setSubject(subject);
        assessment.setTerm(term);
        assessment.setScore(request.score());
        return toResponse(assessmentRepository.save(assessment));
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> findByStudentAndTerm(Long studentId, String term) {
        return assessmentRepository.findByStudentIdAndTermOrderBySubjectSubjectNameAsc(studentId, normalizeTerm(term))
                .stream().map(this::toResponse).toList();
    }

    private String normalizeTerm(String term) {
        String normalized = term == null ? "" : term.trim();
        if (normalized.isEmpty()) {
            throw new BadRequestException("Term is required");
        }
        return normalized;
    }

    private AssessmentResponse toResponse(Assessment assessment) {
        return new AssessmentResponse(
                assessment.getId(),
                assessment.getStudent().getId(),
                assessment.getStudent().getFirstName() + " " + assessment.getStudent().getLastName(),
                assessment.getSubject().getId(),
                assessment.getSubject().getSubjectName(),
                assessment.getTerm(),
                assessment.getScore());
    }
}
