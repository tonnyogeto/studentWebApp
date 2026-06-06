package com.example.StudentWebApp.reporting.dto;

import java.math.BigDecimal;
import java.util.List;

public record StudentResultResponse(
        Long studentId,
        String admissionNumber,
        String studentName,
        Long classStreamId,
        String classStreamName,
        BigDecimal totalScore,
        BigDecimal averageScore,
        String grade,
        Integer rank,
        List<SubjectScoreResponse> subjectScores) {
}
