package com.smartassess.controller;

import com.smartassess.dto.AssessmentDTOs;
import com.smartassess.entity.Assessment;
import com.smartassess.entity.AssessmentQuestion;
import com.smartassess.service.AssessmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final AssessmentService assessmentService;

    public TestController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping("/{shareToken}")
    public ResponseEntity<AssessmentDTOs.PublicTestDTO> getTestByShareToken(@PathVariable String shareToken) {
        Assessment assessment = assessmentService.getAssessmentByShareToken(shareToken);
        List<AssessmentQuestion> questions = assessmentService.getAssessmentQuestions(assessment.getId());
        return ResponseEntity.ok(new AssessmentDTOs.PublicTestDTO(assessment, questions.size()));
    }
}
