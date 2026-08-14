package com.smartassess.controller;

import com.smartassess.entity.Subject;
import com.smartassess.security.UserPrincipal;
import com.smartassess.service.SubjectTopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectTopicService subjectTopicService;

    public SubjectController(SubjectTopicService subjectTopicService) {
        this.subjectTopicService = subjectTopicService;
    }

    @GetMapping
    public ResponseEntity<List<Subject>> getAllSubjects() {
        return ResponseEntity.ok(subjectTopicService.getAllSubjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subject> getSubjectById(@PathVariable Long id) {
        return ResponseEntity.ok(subjectTopicService.getSubjectById(id));
    }

    @PostMapping
    public ResponseEntity<Subject> createSubject(@RequestBody Subject subject, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long facultyId = userPrincipal != null ? userPrincipal.getId() : 1L;
        return ResponseEntity.ok(subjectTopicService.createSubject(subject, facultyId));
    }
}
