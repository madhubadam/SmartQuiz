package com.smartassess.controller;

import com.smartassess.dto.AttemptDTOs;
import com.smartassess.entity.Attempt;
import com.smartassess.security.UserPrincipal;
import com.smartassess.service.AttemptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AttemptController {

    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @PostMapping("/attempts/start")
    public ResponseEntity<AttemptDTOs.StartAttemptResponse> startAttempt(
            @RequestBody AttemptDTOs.StartAttemptRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long studentId = userPrincipal != null ? userPrincipal.getId() : 2L;
        return ResponseEntity.ok(attemptService.startAttempt(request.getAssessmentId(), studentId));
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<AttemptDTOs.AttemptResultResponse> submitAttempt(
            @PathVariable Long attemptId,
            @RequestBody AttemptDTOs.SubmitAttemptRequest submitRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long studentId = userPrincipal != null ? userPrincipal.getId() : 2L;
        return ResponseEntity.ok(attemptService.submitAttempt(attemptId, submitRequest, studentId));
    }

    @GetMapping("/results/{attemptId}")
    public ResponseEntity<AttemptDTOs.AttemptResultResponse> getResult(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : 2L;
        boolean isFaculty = userPrincipal != null && userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_FACULTY"));
        return ResponseEntity.ok(attemptService.getAttemptResult(attemptId, userId, isFaculty));
    }

    @GetMapping("/attempts/my-attempts")
    public ResponseEntity<List<Attempt>> getMyAttempts(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long studentId = userPrincipal != null ? userPrincipal.getId() : 2L;
        return ResponseEntity.ok(attemptService.getStudentAttempts(studentId));
    }
}
