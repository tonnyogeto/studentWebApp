package com.example.StudentWebApp.student.service;

import java.util.List;

import com.example.StudentWebApp.classstream.entity.ClassStream;
import com.example.StudentWebApp.classstream.repository.ClassStreamRepository;
import com.example.StudentWebApp.common.exception.BadRequestException;
import com.example.StudentWebApp.common.exception.ResourceNotFoundException;
import com.example.StudentWebApp.assessment.repository.AssessmentRepository;
import com.example.StudentWebApp.student.dto.StudentRequest;
import com.example.StudentWebApp.student.dto.StudentResponse;
import com.example.StudentWebApp.student.entity.Student;
import com.example.StudentWebApp.student.repository.StudentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final ClassStreamRepository classStreamRepository;
    private final AssessmentRepository assessmentRepository;

    public StudentService(StudentRepository studentRepository, ClassStreamRepository classStreamRepository,
            AssessmentRepository assessmentRepository) {
        this.studentRepository = studentRepository;
        this.classStreamRepository = classStreamRepository;
        this.assessmentRepository = assessmentRepository;
    }

    public StudentResponse create(StudentRequest request) {
        if (studentRepository.findByAdmissionNumber(request.admissionNumber().trim()).isPresent()) {
            throw new BadRequestException("Admission number already exists");
        }
        Student student = new Student();
        applyRequest(student, request);
        return toResponse(studentRepository.save(student));
    }

    public StudentResponse update(Long id, StudentRequest request) {
        Student student = getEntity(id);
        if (studentRepository.existsByAdmissionNumberAndIdNot(request.admissionNumber().trim(), id)) {
            throw new BadRequestException("Admission number already exists");
        }
        applyRequest(student, request);
        return toResponse(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        return studentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    public void delete(Long id) {
        if (assessmentRepository.existsById(id)) {
            throw new BadRequestException("Cannot delete a student that still has assessment records");
        }
        studentRepository.delete(getEntity(id));
    }

    private void applyRequest(Student student, StudentRequest request) {
        ClassStream classStream = classStreamRepository.findById(request.classStreamId())
                .orElseThrow(() -> new ResourceNotFoundException("Class stream not found"));
        student.setAdmissionNumber(request.admissionNumber().trim());
        student.setFirstName(request.firstName().trim());
        student.setLastName(request.lastName().trim());
        student.setGender(request.gender().trim());
        student.setDateOfBirth(request.dateOfBirth());
        student.setClassStream(classStream);
    }

    private Student getEntity(Long id) {
        return studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private StudentResponse toResponse(Student student) {
        ClassStream classStream = student.getClassStream();
        return new StudentResponse(
                student.getId(),
                student.getAdmissionNumber(),
                student.getFirstName(),
                student.getLastName(),
                student.getGender(),
                student.getDateOfBirth(),
                classStream == null ? null : classStream.getId(),
                classStream == null ? null : classStream.getStreamCode(),
                classStream == null ? null : classStream.getStreamName());
    }
}
