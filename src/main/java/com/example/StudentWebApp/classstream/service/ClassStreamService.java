package com.example.StudentWebApp.classstream.service;

import java.util.List;

import com.example.StudentWebApp.classstream.dto.ClassStreamRequest;
import com.example.StudentWebApp.classstream.dto.ClassStreamResponse;
import com.example.StudentWebApp.classstream.entity.ClassStream;
import com.example.StudentWebApp.classstream.repository.ClassStreamRepository;
import com.example.StudentWebApp.common.exception.BadRequestException;
import com.example.StudentWebApp.common.exception.ResourceNotFoundException;
import com.example.StudentWebApp.student.repository.StudentRepository;
import com.example.StudentWebApp.subject.entity.Subject;
import com.example.StudentWebApp.subject.repository.SubjectRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClassStreamService {

    private final ClassStreamRepository classStreamRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;

    public ClassStreamService(ClassStreamRepository classStreamRepository, SubjectRepository subjectRepository,
            StudentRepository studentRepository) {
        this.classStreamRepository = classStreamRepository;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
    }

    public ClassStreamResponse create(ClassStreamRequest request) {
        if (classStreamRepository.findByStreamCode(request.streamCode().trim()).isPresent()) {
            throw new BadRequestException("Class stream code already exists");
        }
        ClassStream classStream = new ClassStream();
        applyRequest(classStream, request);
        return toResponse(classStreamRepository.save(classStream));
    }

    public ClassStreamResponse update(Long id, ClassStreamRequest request) {
        ClassStream classStream = getEntity(id);
        if (classStreamRepository.existsByStreamCodeAndIdNot(request.streamCode().trim(), id)) {
            throw new BadRequestException("Class stream code already exists");
        }
        applyRequest(classStream, request);
        return toResponse(classStreamRepository.save(classStream));
    }

    @Transactional(readOnly = true)
    public List<ClassStreamResponse> findAll() {
        return classStreamRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ClassStreamResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    public void delete(Long id) {
        ClassStream classStream = getEntity(id);
        if (!studentRepository.findByClassStreamIdOrderByLastNameAscFirstNameAsc(id).isEmpty()) {
            throw new BadRequestException("Cannot delete a class stream that still has students assigned");
        }
        if (!classStream.getSubjects().isEmpty()) {
            throw new BadRequestException("Remove assigned subjects before deleting the class stream");
        }
        classStreamRepository.delete(classStream);
    }

    public ClassStreamResponse assignSubject(Long classStreamId, Long subjectId) {
        ClassStream classStream = getEntity(classStreamId);
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        classStream.getSubjects().add(subject);
        subject.getClassStreams().add(classStream);
        return toResponse(classStreamRepository.save(classStream));
    }

    public ClassStreamResponse removeSubject(Long classStreamId, Long subjectId) {
        ClassStream classStream = getEntity(classStreamId);
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        classStream.getSubjects().remove(subject);
        subject.getClassStreams().remove(classStream);
        return toResponse(classStreamRepository.save(classStream));
    }

    private void applyRequest(ClassStream classStream, ClassStreamRequest request) {
        classStream.setStreamCode(request.streamCode().trim());
        classStream.setStreamName(request.streamName().trim());
        classStream.setFormLevel(request.formLevel() == null ? null : request.formLevel().trim());
        classStream.setActive(request.active() == null || request.active());
    }

    private ClassStream getEntity(Long id) {
        return classStreamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class stream not found"));
    }

    private ClassStreamResponse toResponse(ClassStream classStream) {
        return new ClassStreamResponse(
                classStream.getId(),
                classStream.getStreamCode(),
                classStream.getStreamName(),
                classStream.getFormLevel(),
                classStream.isActive(),
                studentRepository.findByClassStreamIdOrderByLastNameAscFirstNameAsc(classStream.getId()).size(),
                classStream.getSubjects().size());
    }
}
