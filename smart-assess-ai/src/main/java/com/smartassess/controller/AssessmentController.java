package com.smartassess.controller;

import com.smartassess.dto.AssessmentDTOs;
import com.smartassess.entity.Assessment;
import com.smartassess.entity.AssessmentQuestion;
import com.smartassess.security.UserPrincipal;
import com.smartassess.service.AssessmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping
    public ResponseEntity<List<Assessment>> getFacultyAssessments(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long facultyId = userPrincipal != null ? userPrincipal.getId() : 1L;
        return ResponseEntity.ok(assessmentService.getFacultyAssessments(facultyId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Assessment> getAssessmentById(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getAssessmentById(id));
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<AssessmentQuestion>> getAssessmentQuestions(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getAssessmentQuestions(id));
    }

    @PostMapping
    public ResponseEntity<Assessment> createAssessment(@RequestBody AssessmentDTOs.AssessmentRequest request, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long facultyId = userPrincipal != null ? userPrincipal.getId() : 1L;
        return ResponseEntity.ok(assessmentService.createAssessment(request, facultyId));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Assessment> publishAssessment(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.publishAssessment(id));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<Assessment> closeAssessment(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.closeAssessment(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssessment(@PathVariable Long id) {
        assessmentService.deleteAssessment(id);
        return ResponseEntity.noContent().build();
    }
}
