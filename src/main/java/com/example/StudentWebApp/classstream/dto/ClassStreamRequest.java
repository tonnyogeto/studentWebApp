package com.example.StudentWebApp.classstream.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClassStreamRequest(
        @NotBlank(message = "Stream code is required") @Size(max = 50, message = "Stream code must be at most 50 characters") String streamCode,

        @NotBlank(message = "Stream name is required") @Size(max = 150, message = "Stream name must be at most 150 characters") String streamName,

        @Size(max = 50, message = "Form level must be at most 50 characters") String formLevel,

        Boolean active) {
}
