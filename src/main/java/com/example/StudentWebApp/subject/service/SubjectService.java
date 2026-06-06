package com.example.StudentWebApp.subject.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.StudentWebApp.assessment.repository.AssessmentRepository;
import com.example.StudentWebApp.common.exception.BadRequestException;
import com.example.StudentWebApp.common.exception.ResourceNotFoundException;
import com.example.StudentWebApp.subject.dto.SubjectRequest;
import com.example.StudentWebApp.subject.dto.SubjectResponse;
import com.example.StudentWebApp.subject.entity.Subject;
import com.example.StudentWebApp.subject.repository.SubjectRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final AssessmentRepository assessmentRepository;

    public SubjectService(SubjectRepository subjectRepository, AssessmentRepository assessmentRepository) {
        this.subjectRepository = subjectRepository;
        this.assessmentRepository = assessmentRepository;
    }

    public SubjectResponse create(SubjectRequest request) {
        if (subjectRepository.findBySubjectCode(request.subjectCode().trim()).isPresent()) {
            throw new BadRequestException("Subject code already exists");
        }
        if (subjectRepository.findAll().stream()
                .anyMatch(subject -> subject.getSubjectName().equalsIgnoreCase(request.subjectName().trim()))) {
            throw new BadRequestException("Subject name already exists");
        }
        Subject subject = new Subject();
        applyRequest(subject, request);
        return toResponse(subjectRepository.save(subject));
    }

    public SubjectResponse update(Long id, SubjectRequest request) {
        Subject subject = getEntity(id);
        if (subjectRepository.existsBySubjectCodeAndIdNot(request.subjectCode().trim(), id)) {
            throw new BadRequestException("Subject code already exists");
        }
        if (subjectRepository.findAll().stream().anyMatch(other -> !other.getId().equals(id)
                && other.getSubjectName().equalsIgnoreCase(request.subjectName().trim()))) {
            throw new BadRequestException("Subject name already exists");
        }
        applyRequest(subject, request);
        return toResponse(subjectRepository.save(subject));
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> findAll() {
        return subjectRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubjectResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    public void delete(Long id) {
        Subject subject = getEntity(id);
        if (!subject.getClassStreams().isEmpty()) {
            throw new BadRequestException("Remove assigned class streams before deleting the subject");
        }
        if (assessmentRepository.existsBySubjectId(id)) {
            throw new BadRequestException("Cannot delete a subject that still has assessment records");
        }
        subjectRepository.delete(subject);
    }

    private void applyRequest(Subject subject, SubjectRequest request) {
        subject.setSubjectCode(request.subjectCode().trim());
        subject.setSubjectName(request.subjectName().trim());
    }

    private Subject getEntity(Long id) {
        return subjectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
    }

    private SubjectResponse toResponse(Subject subject) {
        Set<Long> classStreamIds = subject.getClassStreams().stream().map(stream -> stream.getId())
                .collect(Collectors.toSet());
        return new SubjectResponse(subject.getId(), subject.getSubjectCode(), subject.getSubjectName(), classStreamIds,
                classStreamIds.size());
    }
}
