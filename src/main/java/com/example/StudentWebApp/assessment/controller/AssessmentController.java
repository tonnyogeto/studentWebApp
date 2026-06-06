package com.example.StudentWebApp.assessment.controller;

import java.util.List;

import com.example.StudentWebApp.assessment.dto.AssessmentRequest;
import com.example.StudentWebApp.assessment.dto.AssessmentResponse;
import com.example.StudentWebApp.assessment.service.AssessmentService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping
    public ResponseEntity<AssessmentResponse> record(@Valid @RequestBody AssessmentRequest request) {
        return ResponseEntity.ok(assessmentService.record(request));
    }

    @GetMapping
    public List<AssessmentResponse> findByStudentAndTerm(@RequestParam Long studentId, @RequestParam String term) {
        return assessmentService.findByStudentAndTerm(studentId, term);
    }
}
