package com.example.StudentWebApp.classstream.entity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.example.StudentWebApp.student.entity.Student;
import com.example.StudentWebApp.subject.entity.Subject;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "class_streams", uniqueConstraints = @UniqueConstraint(name = "uk_class_stream_code", columnNames = "stream_code"))
public class ClassStream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stream_code", nullable = false, length = 50)
    private String streamCode;

    @Column(name = "stream_name", nullable = false, length = 150)
    private String streamName;

    @Column(name = "form_level", length = 50)
    private String formLevel;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "classStream", fetch = FetchType.LAZY)
    private List<Student> students = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "class_stream_subjects", joinColumns = @JoinColumn(name = "class_stream_id"), inverseJoinColumns = @JoinColumn(name = "subject_id"))
    private Set<Subject> subjects = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStreamCode() {
        return streamCode;
    }

    public void setStreamCode(String streamCode) {
        this.streamCode = streamCode;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getFormLevel() {
        return formLevel;
    }

    public void setFormLevel(String formLevel) {
        this.formLevel = formLevel;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public Set<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(Set<Subject> subjects) {
        this.subjects = subjects;
    }
}
