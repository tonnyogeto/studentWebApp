package com.example.StudentWebApp.subject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubjectRequest(
        @NotBlank(message = "Subject code is required") @Size(max = 50, message = "Subject code must be at most 50 characters") String subjectCode,

        @NotBlank(message = "Subject name is required") @Size(max = 150, message = "Subject name must be at most 150 characters") String subjectName) {
}
