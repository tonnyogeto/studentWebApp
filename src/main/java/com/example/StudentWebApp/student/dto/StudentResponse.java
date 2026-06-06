package com.example.StudentWebApp.student.dto;

import java.time.LocalDate;

public record StudentResponse(
        Long id,
        String admissionNumber,
        String firstName,
        String lastName,
        String gender,
        LocalDate dateOfBirth,
        Long classStreamId,
        String classStreamCode,
        String classStreamName) {
}
