package com.smartassess.controller;

import com.smartassess.dto.QuestionDTOs;
import com.smartassess.entity.Question;
import com.smartassess.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ResponseEntity<List<Question>> searchQuestions(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) Question.Difficulty difficulty,
            @RequestParam(required = false) Question.Source source,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(questionService.searchQuestions(subjectId, topicId, difficulty, source, query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Question> getQuestionById(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestionById(id));
    }

    @PostMapping
    public ResponseEntity<Question> createQuestion(@Valid @RequestBody QuestionDTOs.QuestionRequest request) {
        return ResponseEntity.ok(questionService.createQuestion(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Question> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionDTOs.QuestionRequest request) {
        return ResponseEntity.ok(questionService.updateQuestion(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
