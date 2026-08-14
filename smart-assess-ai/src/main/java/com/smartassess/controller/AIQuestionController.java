package com.smartassess.controller;

import com.smartassess.dto.QuestionDTOs;
import com.smartassess.service.AIService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIQuestionController {

    private final AIService aiService;

    public AIQuestionController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/generate-questions")
    public ResponseEntity<QuestionDTOs.AIGenerateResponse> generateQuestions(@Valid @RequestBody QuestionDTOs.AIGenerateRequest request) {
        return ResponseEntity.ok(aiService.generateQuestions(request));
    }
}
