package com.example.StudentWebApp.student.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record StudentRequest(
        @NotBlank(message = "Admission number is required") @Size(max = 50, message = "Admission number must be at most 50 characters") String admissionNumber,

        @NotBlank(message = "First name is required") @Size(max = 100, message = "First name must be at most 100 characters") String firstName,

        @NotBlank(message = "Last name is required") @Size(max = 100, message = "Last name must be at most 100 characters") String lastName,

        @NotBlank(message = "Gender is required") @Size(max = 20, message = "Gender must be at most 20 characters") String gender,

        @PastOrPresent(message = "Date of birth must be in the past or present") LocalDate dateOfBirth,

        @NotNull(message = "Class stream is required") Long classStreamId) {
}
