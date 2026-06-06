package com.example.StudentWebApp.reporting.dto;

import java.math.BigDecimal;
import java.util.List;

public record ClassStreamResultResponse(
        Long classStreamId,
        String classStreamCode,
        String classStreamName,
        String term,
        BigDecimal classAverage,
        List<StudentResultResponse> studentResults) {
}
