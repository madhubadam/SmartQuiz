package com.smartassess.service;

import com.smartassess.dto.AnalyticsDTOs;
import com.smartassess.entity.*;
import com.smartassess.exception.ResourceNotFoundException;
import com.smartassess.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final AssessmentRepository assessmentRepository;
    private final AttemptRepository attemptRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final AIService aiService;

    public AnalyticsService(AssessmentRepository assessmentRepository,
                            AttemptRepository attemptRepository,
                            AssessmentQuestionRepository assessmentQuestionRepository,
                            StudentAnswerRepository studentAnswerRepository,
                            AIService aiService) {
        this.assessmentRepository = assessmentRepository;
        this.attemptRepository = attemptRepository;
        this.assessmentQuestionRepository = assessmentQuestionRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.aiService = aiService;
    }

    public AnalyticsDTOs.FacultyAnalyticsResponse getAssessmentAnalytics(Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id " + assessmentId));

        List<Attempt> attempts = attemptRepository.findByAssessmentId(assessmentId)
                .stream()
                .filter(a -> a.getStatus() == Attempt.Status.COMPLETED)
                .collect(Collectors.toList());

        AnalyticsDTOs.FacultyAnalyticsResponse response = new AnalyticsDTOs.FacultyAnalyticsResponse();
        response.setAssessmentId(assessment.getId());
        response.setAssessmentTitle(assessment.getTitle());
        response.setTotalAttempts(attempts.size());

        if (attempts.isEmpty()) {
            response.setTotalStudentsAttempted(0);
            response.setAverageScore(0.0);
            response.setHighestScore(0);
            response.setLowestScore(0);
            response.setPassPercentage(0.0);
            response.setStudentPerformances(Collections.emptyList());
            response.setQuestionPerformances(Collections.emptyList());
            response.setTopicPerformances(Collections.emptyList());
            response.setAiInsights("No student attempts have been submitted for this assessment yet.");
            return response;
        }

        Set<Long> uniqueStudentIds = attempts.stream().map(a -> a.getStudent().getId()).collect(Collectors.toSet());
        response.setTotalStudentsAttempted(uniqueStudentIds.size());

        double avgScore = attempts.stream().mapToInt(Attempt::getScore).average().orElse(0.0);
        int maxScore = attempts.stream().mapToInt(Attempt::getScore).max().orElse(0);
        int minScore = attempts.stream().mapToInt(Attempt::getScore).min().orElse(0);
        long passedCount = attempts.stream().filter(a -> a.getPercentage() >= 50.0).count();
        double passPct = ((double) passedCount / attempts.size()) * 100.0;

        response.setAverageScore(Math.round(avgScore * 10.0) / 10.0);
        response.setHighestScore(maxScore);
        response.setLowestScore(minScore);
        response.setPassPercentage(Math.round(passPct * 10.0) / 10.0);

        // Student Performance Table
        Map<Long, List<Attempt>> studentAttemptsMap = attempts.stream().collect(Collectors.groupingBy(a -> a.getStudent().getId()));
        List<AnalyticsDTOs.StudentPerformanceDTO> studentList = new ArrayList<>();

        for (Map.Entry<Long, List<Attempt>> entry : studentAttemptsMap.entrySet()) {
            List<Attempt> sAttempts = entry.getValue();
            Attempt latest = sAttempts.get(sAttempts.size() - 1);
            User student = latest.getStudent();
            String status = latest.getPercentage() >= 50.0 ? "PASSED" : "FAILED";

            studentList.add(new AnalyticsDTOs.StudentPerformanceDTO(
                    student.getName(),
                    student.getEmail(),
                    latest.getScore(),
                    latest.getPercentage(),
                    sAttempts.size(),
                    status
            ));
        }
        response.setStudentPerformances(studentList);

        // Question Performance
        List<AssessmentQuestion> aqList = assessmentQuestionRepository.findByAssessmentIdOrderByQuestionOrderAsc(assessmentId);
        List<AnalyticsDTOs.QuestionPerformanceDTO> questionPerfList = new ArrayList<>();
        Map<String, List<Double>> topicScoresMap = new HashMap<>();

        for (AssessmentQuestion aq : aqList) {
            Question q = aq.getQuestion();
            List<StudentAnswer> answers = studentAnswerRepository.findByQuestionId(q.getId())
                    .stream()
                    .filter(sa -> attempts.stream().anyMatch(att -> att.getId().equals(sa.getAttempt().getId())))
                    .collect(Collectors.toList());

            int qAttempts = answers.size();
            long qCorrect = answers.stream().filter(StudentAnswer::isCorrect).count();
            double correctPct = qAttempts > 0 ? ((double) qCorrect / qAttempts) * 100.0 : 0.0;
            double wrongPct = qAttempts > 0 ? 100.0 - correctPct : 0.0;

            String topicName = q.getTopic() != null ? q.getTopic().getName() : (q.getSubject() != null ? q.getSubject().getName() : "General");

            topicScoresMap.computeIfAbsent(topicName, k -> new ArrayList<>()).add(correctPct);

            questionPerfList.add(new AnalyticsDTOs.QuestionPerformanceDTO(
                    q.getId(),
                    q.getQuestionText(),
                    topicName,
                    q.getDifficulty().name(),
                    qAttempts,
                    Math.round(correctPct * 10.0) / 10.0,
                    Math.round(wrongPct * 10.0) / 10.0
            ));
        }
        response.setQuestionPerformances(questionPerfList);

        // Topic Performance Aggregation
        List<AnalyticsDTOs.TopicPerformanceDTO> topicPerfList = new ArrayList<>();
        List<String> topicSummaries = new ArrayList<>();

        for (Map.Entry<String, List<Double>> entry : topicScoresMap.entrySet()) {
            double avgTopicPct = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            avgTopicPct = Math.round(avgTopicPct * 10.0) / 10.0;
            topicPerfList.add(new AnalyticsDTOs.TopicPerformanceDTO(entry.getKey(), avgTopicPct));
            topicSummaries.add(entry.getKey() + ": " + avgTopicPct + "% average");
        }

        // Sort topic performance descending
        topicPerfList.sort((a, b) -> Double.compare(b.getAveragePercentage(), a.getAveragePercentage()));
        response.setTopicPerformances(topicPerfList);

        // Generate AI Performance Insights
        String insights = aiService.generateClassInsights(assessment.getTitle(), uniqueStudentIds.size(), topicSummaries);
        response.setAiInsights(insights);

        return response;
    }
}
