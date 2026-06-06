package com.example.StudentWebApp.reporting.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.StudentWebApp.assessment.entity.Assessment;
import com.example.StudentWebApp.assessment.repository.AssessmentRepository;
import com.example.StudentWebApp.classstream.entity.ClassStream;
import com.example.StudentWebApp.classstream.repository.ClassStreamRepository;
import com.example.StudentWebApp.common.exception.BadRequestException;
import com.example.StudentWebApp.common.exception.ResourceNotFoundException;
import com.example.StudentWebApp.common.util.GradeCalculator;
import com.example.StudentWebApp.reporting.dto.ClassStreamResultResponse;
import com.example.StudentWebApp.reporting.dto.StudentResultResponse;
import com.example.StudentWebApp.reporting.dto.SubjectScoreResponse;
import com.example.StudentWebApp.student.entity.Student;
import com.example.StudentWebApp.student.repository.StudentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ResultsService {

    private final ClassStreamRepository classStreamRepository;
    private final StudentRepository studentRepository;
    private final AssessmentRepository assessmentRepository;

    public ResultsService(ClassStreamRepository classStreamRepository, StudentRepository studentRepository,
            AssessmentRepository assessmentRepository) {
        this.classStreamRepository = classStreamRepository;
        this.studentRepository = studentRepository;
        this.assessmentRepository = assessmentRepository;
    }

    public ClassStreamResultResponse getClassStreamResults(Long classStreamId, String term) {
        ClassStream classStream = classStreamRepository.findById(classStreamId)
                .orElseThrow(() -> new ResourceNotFoundException("Class stream not found"));
        String normalizedTerm = normalizeTerm(term);
        List<Student> students = studentRepository.findByClassStreamIdOrderByLastNameAscFirstNameAsc(classStreamId);
        List<Assessment> assessments = assessmentRepository.findForClassStreamAndTerm(classStreamId, normalizedTerm);
        Map<Long, List<Assessment>> assessmentsByStudent = assessments.stream().collect(Collectors
                .groupingBy(assessment -> assessment.getStudent().getId(), LinkedHashMap::new, Collectors.toList()));
        List<StudentResultResponse> results = new ArrayList<>();
        for (Student student : students) {
            results.add(buildStudentResult(student, assessmentsByStudent.getOrDefault(student.getId(), List.of())));
        }
        results.sort(Comparator.comparing(StudentResultResponse::totalScore).reversed()
                .thenComparing(StudentResultResponse::averageScore, Comparator.reverseOrder())
                .thenComparing(StudentResultResponse::studentName));
        assignRanks(results);
        BigDecimal classAverage = results.isEmpty()
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : results.stream().map(StudentResultResponse::averageScore).reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(results.size()), 2, RoundingMode.HALF_UP);
        return new ClassStreamResultResponse(classStream.getId(), classStream.getStreamCode(),
                classStream.getStreamName(), normalizedTerm, classAverage, results);
    }

    public StudentResultResponse getStudentResult(Long studentId, String term) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (student.getClassStream() == null) {
            throw new BadRequestException("Student is not assigned to a class stream");
        }
        ClassStreamResultResponse classResults = getClassStreamResults(student.getClassStream().getId(), term);
        return classResults.studentResults().stream().filter(result -> result.studentId().equals(studentId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Student result not found"));
    }

    private StudentResultResponse buildStudentResult(Student student, List<Assessment> assessments) {
        List<SubjectScoreResponse> scores = assessments.stream()
                .map(assessment -> new SubjectScoreResponse(assessment.getSubject().getId(),
                        assessment.getSubject().getSubjectName(), assessment.getScore()))
                .toList();
        BigDecimal total = assessments.stream().map(Assessment::getScore).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal average = assessments.isEmpty()
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : total.divide(BigDecimal.valueOf(assessments.size()), 2, RoundingMode.HALF_UP);
        return new StudentResultResponse(
                student.getId(),
                student.getAdmissionNumber(),
                student.getFirstName() + " " + student.getLastName(),
                student.getClassStream().getId(),
                student.getClassStream().getStreamName(),
                total,
                average,
                GradeCalculator.grade(average),
                null,
                scores);
    }

    private void assignRanks(List<StudentResultResponse> results) {
        BigDecimal previousTotal = null;
        BigDecimal previousAverage = null;
        int previousRank = 0;
        for (int index = 0; index < results.size(); index++) {
            StudentResultResponse result = results.get(index);
            int rank = index + 1;
            if (previousTotal != null && previousTotal.compareTo(result.totalScore()) == 0 && previousAverage != null
                    && previousAverage.compareTo(result.averageScore()) == 0) {
                rank = previousRank;
            }
            results.set(index, new StudentResultResponse(
                    result.studentId(),
                    result.admissionNumber(),
                    result.studentName(),
                    result.classStreamId(),
                    result.classStreamName(),
                    result.totalScore(),
                    result.averageScore(),
                    result.grade(),
                    rank,
                    result.subjectScores()));
            previousTotal = result.totalScore();
            previousAverage = result.averageScore();
            previousRank = rank;
        }
    }

    private String normalizeTerm(String term) {
        String normalized = term == null ? "" : term.trim();
        if (normalized.isEmpty()) {
            throw new BadRequestException("Term is required");
        }
        return normalized;
    }
}
