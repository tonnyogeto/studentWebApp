package com.example.StudentWebApp.subject.dto;

import java.util.Set;

public record SubjectResponse(
        Long id,
        String subjectCode,
        String subjectName,
        Set<Long> classStreamIds,
        int classStreamCount) {
}
