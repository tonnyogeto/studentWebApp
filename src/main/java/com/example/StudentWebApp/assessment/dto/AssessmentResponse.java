package com.example.StudentWebApp.assessment.dto;

import java.math.BigDecimal;

public record AssessmentResponse(
        Long id,
        Long studentId,
        String studentName,
        Long subjectId,
        String subjectName,
        String term,
        BigDecimal score) {
}
