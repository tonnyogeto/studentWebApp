package com.example.StudentWebApp.subject.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import com.example.StudentWebApp.assessment.entity.Assessment;
import com.example.StudentWebApp.classstream.entity.ClassStream;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "subjects", uniqueConstraints = {
        @UniqueConstraint(name = "uk_subject_code", columnNames = "subject_code"),
        @UniqueConstraint(name = "uk_subject_name", columnNames = "subject_name")
})
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_code", nullable = false, length = 50)
    private String subjectCode;

    @Column(name = "subject_name", nullable = false, length = 150)
    private String subjectName;

    @ManyToMany(mappedBy = "subjects", fetch = FetchType.LAZY)
    private Set<ClassStream> classStreams = new LinkedHashSet<>();

    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY)
    private Set<Assessment> assessments = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Set<ClassStream> getClassStreams() {
        return classStreams;
    }

    public void setClassStreams(Set<ClassStream> classStreams) {
        this.classStreams = classStreams;
    }

    public Set<Assessment> getAssessments() {
        return assessments;
    }

    public void setAssessments(Set<Assessment> assessments) {
        this.assessments = assessments;
    }
}
