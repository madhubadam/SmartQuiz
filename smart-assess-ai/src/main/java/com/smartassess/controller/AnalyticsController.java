package com.smartassess.controller;

import com.smartassess.dto.AnalyticsDTOs;
import com.smartassess.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/assessment/{assessmentId}")
    public ResponseEntity<AnalyticsDTOs.FacultyAnalyticsResponse> getAssessmentAnalytics(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(analyticsService.getAssessmentAnalytics(assessmentId));
    }

    @GetMapping("/class/{assessmentId}/insights")
    public ResponseEntity<String> getClassInsights(@PathVariable Long assessmentId) {
        AnalyticsDTOs.FacultyAnalyticsResponse response = analyticsService.getAssessmentAnalytics(assessmentId);
        return ResponseEntity.ok(response.getAiInsights());
    }
}
