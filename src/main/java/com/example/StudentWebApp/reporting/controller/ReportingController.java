package com.example.StudentWebApp.reporting.controller;

import com.example.StudentWebApp.reporting.service.ReportingService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping(value = "/students/{studentId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> studentReport(@PathVariable Long studentId,
            @RequestParam(defaultValue = "Term 1") String term) {
        byte[] report = reportingService.generateStudentReport(studentId, term);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(report);
    }

    @GetMapping(value = "/class-streams/{classStreamId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> classReport(@PathVariable Long classStreamId,
            @RequestParam(defaultValue = "Term 1") String term) {
        byte[] report = reportingService.generateClassReport(classStreamId, term);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=class-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(report);
    }
}
