package com.smartassess.dto;

import java.util.List;

public class AnalyticsDTOs {

    public static class FacultyAnalyticsResponse {
        private Long assessmentId;
        private String assessmentTitle;
        private int totalStudentsAttempted;
        private int totalAttempts;
        private double averageScore;
        private int highestScore;
        private int lowestScore;
        private double passPercentage;
        private List<StudentPerformanceDTO> studentPerformances;
        private List<QuestionPerformanceDTO> questionPerformances;
        private List<TopicPerformanceDTO> topicPerformances;
        private String aiInsights;

        public Long getAssessmentId() { return assessmentId; }
        public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

        public String getAssessmentTitle() { return assessmentTitle; }
        public void setAssessmentTitle(String assessmentTitle) { this.assessmentTitle = assessmentTitle; }

        public int getTotalStudentsAttempted() { return totalStudentsAttempted; }
        public void setTotalStudentsAttempted(int totalStudentsAttempted) { this.totalStudentsAttempted = totalStudentsAttempted; }

        public int getTotalAttempts() { return totalAttempts; }
        public void setTotalAttempts(int totalAttempts) { this.totalAttempts = totalAttempts; }

        public double getAverageScore() { return averageScore; }
        public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

        public int getHighestScore() { return highestScore; }
        public void setHighestScore(int highestScore) { this.highestScore = highestScore; }

        public int getLowestScore() { return lowestScore; }
        public void setLowestScore(int lowestScore) { this.lowestScore = lowestScore; }

        public double getPassPercentage() { return passPercentage; }
        public void setPassPercentage(double passPercentage) { this.passPercentage = passPercentage; }

        public List<StudentPerformanceDTO> getStudentPerformances() { return studentPerformances; }
        public void setStudentPerformances(List<StudentPerformanceDTO> studentPerformances) { this.studentPerformances = studentPerformances; }

        public List<QuestionPerformanceDTO> getQuestionPerformances() { return questionPerformances; }
        public void setQuestionPerformances(List<QuestionPerformanceDTO> questionPerformances) { this.questionPerformances = questionPerformances; }

        public List<TopicPerformanceDTO> getTopicPerformances() { return topicPerformances; }
        public void setTopicPerformances(List<TopicPerformanceDTO> topicPerformances) { this.topicPerformances = topicPerformances; }

        public String getAiInsights() { return aiInsights; }
        public void setAiInsights(String aiInsights) { this.aiInsights = aiInsights; }
    }

    public static class StudentPerformanceDTO {
        private String studentName;
        private String email;
        private int score;
        private double percentage;
        private int attemptsCount;
        private String status;

        public StudentPerformanceDTO(String studentName, String email, int score, double percentage, int attemptsCount, String status) {
            this.studentName = studentName;
            this.email = email;
            this.score = score;
            this.percentage = percentage;
            this.attemptsCount = attemptsCount;
            this.status = status;
        }

        public String getStudentName() { return studentName; }
        public String getEmail() { return email; }
        public int getScore() { return score; }
        public double getPercentage() { return percentage; }
        public int getAttemptsCount() { return attemptsCount; }
        public String getStatus() { return status; }
    }

    public static class QuestionPerformanceDTO {
        private Long questionId;
        private String questionText;
        private String topicName;
        private String difficulty;
        private int totalAttempts;
        private double correctPercentage;
        private double wrongPercentage;

        public QuestionPerformanceDTO(Long questionId, String questionText, String topicName, String difficulty, int totalAttempts, double correctPercentage, double wrongPercentage) {
            this.questionId = questionId;
            this.questionText = questionText;
            this.topicName = topicName;
            this.difficulty = difficulty;
            this.totalAttempts = totalAttempts;
            this.correctPercentage = correctPercentage;
            this.wrongPercentage = wrongPercentage;
        }

        public Long getQuestionId() { return questionId; }
        public String getQuestionText() { return questionText; }
        public String getTopicName() { return topicName; }
        public String getDifficulty() { return difficulty; }
        public int getTotalAttempts() { return totalAttempts; }
        public double getCorrectPercentage() { return correctPercentage; }
        public double getWrongPercentage() { return wrongPercentage; }
    }

    public static class TopicPerformanceDTO {
        private String topicName;
        private double averagePercentage;

        public TopicPerformanceDTO(String topicName, double averagePercentage) {
            this.topicName = topicName;
            this.averagePercentage = averagePercentage;
        }

        public String getTopicName() { return topicName; }
        public double getAveragePercentage() { return averagePercentage; }
    }
}
