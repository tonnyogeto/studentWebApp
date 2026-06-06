package com.example.StudentWebApp.reporting.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.example.StudentWebApp.reporting.dto.ClassStreamResultResponse;
import com.example.StudentWebApp.reporting.dto.StudentResultResponse;
import com.example.StudentWebApp.reporting.dto.SubjectScoreResponse;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

@Service
public class ReportingService {

    private final ResultsService resultsService;

    public ReportingService(ResultsService resultsService) {
        this.resultsService = resultsService;
    }

    public byte[] generateStudentReport(Long studentId, String term) {
        StudentResultResponse result = resultsService.getStudentResult(studentId, term);
        return renderDocument("Ikonex Academy Student Report Card", buildStudentReportLines(result, term));
    }

    public byte[] generateClassReport(Long classStreamId, String term) {
        ClassStreamResultResponse response = resultsService.getClassStreamResults(classStreamId, term);
        return renderDocument("Ikonex Academy Class Report", buildClassReportLines(response));
    }

    private byte[] renderDocument(String title, List<String> lines) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.newLineAtOffset(50, 780);
                contentStream.showText(title);
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.newLineAtOffset(0, -24);
                for (String line : lines) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -16);
                }
                contentStream.endText();
            }
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate PDF report", exception);
        }
    }

    private List<String> buildStudentReportLines(StudentResultResponse result, String term) {
        List<String> lines = new ArrayList<>();
        lines.add("Student: " + result.studentName() + " | Admission No: " + result.admissionNumber());
        lines.add("Class Stream: " + result.classStreamName());
        lines.add("Term: " + term);
        lines.add(" ");
        lines.add("Subject                 Score");
        lines.add("--------------------------------");
        for (SubjectScoreResponse score : result.subjectScores()) {
            lines.add(formatLine(score.subjectName(), score.score().toPlainString()));
        }
        lines.add(" ");
        lines.add("Total: " + result.totalScore().setScale(2, RoundingMode.HALF_UP));
        lines.add("Average: " + result.averageScore().setScale(2, RoundingMode.HALF_UP));
        lines.add("Grade: " + result.grade());
        lines.add("Rank: " + (result.rank() == null ? "-" : result.rank()));
        return lines;
    }

    private List<String> buildClassReportLines(ClassStreamResultResponse response) {
        List<String> lines = new ArrayList<>();
        lines.add("Class Stream: " + response.classStreamName() + " | Term: " + response.term());
        lines.add("Average: " + response.classAverage().setScale(2, RoundingMode.HALF_UP));
        lines.add(" ");
        lines.add("Student                       Total   Avg   Grade Rank");
        lines.add("-------------------------------------------------------");
        for (StudentResultResponse result : response.studentResults()) {
            lines.add(String.format("%-28s %6s %6s %5s %4s", truncate(result.studentName(), 28),
                    result.totalScore().setScale(2, RoundingMode.HALF_UP),
                    result.averageScore().setScale(2, RoundingMode.HALF_UP), result.grade(),
                    result.rank() == null ? "-" : result.rank().toString()));
        }
        return lines;
    }

    private String formatLine(String label, String score) {
        return String.format("%-24s %6s", truncate(label, 24), score);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }
}
