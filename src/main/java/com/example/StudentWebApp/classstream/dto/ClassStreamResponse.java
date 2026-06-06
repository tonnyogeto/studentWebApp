package com.example.StudentWebApp.classstream.dto;

public record ClassStreamResponse(
        Long id,
        String streamCode,
        String streamName,
        String formLevel,
        boolean active,
        long studentCount,
        long subjectCount) {
}
