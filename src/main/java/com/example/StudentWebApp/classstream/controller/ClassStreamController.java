package com.example.StudentWebApp.classstream.controller;

import java.util.List;

import com.example.StudentWebApp.classstream.dto.ClassStreamRequest;
import com.example.StudentWebApp.classstream.dto.ClassStreamResponse;
import com.example.StudentWebApp.classstream.service.ClassStreamService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/class-streams")
public class ClassStreamController {

    private final ClassStreamService classStreamService;

    public ClassStreamController(ClassStreamService classStreamService) {
        this.classStreamService = classStreamService;
    }

    @GetMapping
    public List<ClassStreamResponse> findAll() {
        return classStreamService.findAll();
    }

    @GetMapping("/{id}")
    public ClassStreamResponse findById(@PathVariable Long id) {
        return classStreamService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ClassStreamResponse> create(@Valid @RequestBody ClassStreamRequest request) {
        return ResponseEntity.ok(classStreamService.create(request));
    }

    @PutMapping("/{id}")
    public ClassStreamResponse update(@PathVariable Long id, @Valid @RequestBody ClassStreamRequest request) {
        return classStreamService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        classStreamService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{classStreamId}/subjects/{subjectId}")
    public ClassStreamResponse assignSubject(@PathVariable Long classStreamId, @PathVariable Long subjectId) {
        return classStreamService.assignSubject(classStreamId, subjectId);
    }

    @DeleteMapping("/{classStreamId}/subjects/{subjectId}")
    public ClassStreamResponse removeSubject(@PathVariable Long classStreamId, @PathVariable Long subjectId) {
        return classStreamService.removeSubject(classStreamId, subjectId);
    }
}
