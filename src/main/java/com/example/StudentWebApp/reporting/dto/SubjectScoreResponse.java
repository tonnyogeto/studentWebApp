package com.example.StudentWebApp.reporting.dto;

import java.math.BigDecimal;

public record SubjectScoreResponse(
        Long subjectId,
        String subjectName,
        BigDecimal score) {
}
