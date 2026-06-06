package com.example.StudentWebApp.reporting.controller;

import com.example.StudentWebApp.reporting.dto.ClassStreamResultResponse;
import com.example.StudentWebApp.reporting.dto.StudentResultResponse;
import com.example.StudentWebApp.reporting.service.ResultsService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/results")
public class ResultsController {

    private final ResultsService resultsService;

    public ResultsController(ResultsService resultsService) {
        this.resultsService = resultsService;
    }

    @GetMapping("/class-streams/{classStreamId}")
    public ClassStreamResultResponse getClassStreamResults(@PathVariable Long classStreamId,
            @RequestParam(defaultValue = "Term 1") String term) {
        return resultsService.getClassStreamResults(classStreamId, term);
    }

    @GetMapping("/students/{studentId}")
    public StudentResultResponse getStudentResults(@PathVariable Long studentId,
            @RequestParam(defaultValue = "Term 1") String term) {
        return resultsService.getStudentResult(studentId, term);
    }
}
