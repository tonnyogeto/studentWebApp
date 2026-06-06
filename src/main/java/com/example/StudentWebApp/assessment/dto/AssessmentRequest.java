package com.example.StudentWebApp.assessment.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssessmentRequest(
        @NotNull(message = "Student is required") Long studentId,

        @NotNull(message = "Subject is required") Long subjectId,

        @NotBlank(message = "Term is required") @Size(max = 50, message = "Term must be at most 50 characters") String term,

        @NotNull(message = "Score is required") @DecimalMin(value = "0.0", inclusive = true, message = "Score must be between 0 and 100") @DecimalMax(value = "100.0", inclusive = true, message = "Score must be between 0 and 100") BigDecimal score) {
}
