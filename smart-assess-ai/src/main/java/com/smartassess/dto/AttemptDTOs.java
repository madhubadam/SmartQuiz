package com.smartassess.dto;

import java.util.List;

public class AttemptDTOs {

    public static class StartAttemptRequest {
        private Long assessmentId;
        public Long getAssessmentId() { return assessmentId; }
        public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }
    }

    public static class ClientQuestionDTO {
        private Long id;
        private String questionText;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private Integer marks;

        public ClientQuestionDTO(Long id, String questionText, String optionA, String optionB, String optionC, String optionD, Integer marks) {
            this.id = id;
            this.questionText = questionText;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.marks = marks;
        }

        public Long getId() { return id; }
        public String getQuestionText() { return questionText; }
        public String getOptionA() { return optionA; }
        public String getOptionB() { return optionB; }
        public String getOptionC() { return optionC; }
        public String getOptionD() { return optionD; }
        public Integer getMarks() { return marks; }
    }

    public static class StartAttemptResponse {
        private Long attemptId;
        private String assessmentTitle;
        private String subjectName;
        private Integer durationSeconds;
        private List<ClientQuestionDTO> questions;

        public StartAttemptResponse(Long attemptId, String assessmentTitle, String subjectName, Integer durationSeconds, List<ClientQuestionDTO> questions) {
            this.attemptId = attemptId;
            this.assessmentTitle = assessmentTitle;
            this.subjectName = subjectName;
            this.durationSeconds = durationSeconds;
            this.questions = questions;
        }

        public Long getAttemptId() { return attemptId; }
        public String getAssessmentTitle() { return assessmentTitle; }
        public String getSubjectName() { return subjectName; }
        public Integer getDurationSeconds() { return durationSeconds; }
        public List<ClientQuestionDTO> getQuestions() { return questions; }
    }

    public static class SubmitAnswerDTO {
        private Long questionId;
        private String selectedAnswer; // A, B, C, or D, or null

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }

        public String getSelectedAnswer() { return selectedAnswer; }
        public void setSelectedAnswer(String selectedAnswer) { this.selectedAnswer = selectedAnswer; }
    }

    public static class SubmitAttemptRequest {
        private List<SubmitAnswerDTO> answers;
        public List<SubmitAnswerDTO> getAnswers() { return answers; }
        public void setAnswers(List<SubmitAnswerDTO> answers) { this.answers = answers; }
    }

    public static class AttemptResultResponse {
        private Long attemptId;
        private Integer score;
        private Integer totalMarks;
        private Double percentage;
        private int correct;
        private int wrong;
        private int unanswered;
        private String timeTaken;
        private String performanceLevel;
        private String message;
        private List<QuestionResultDTO> questionResults;

        public AttemptResultResponse() {}

        public Long getAttemptId() { return attemptId; }
        public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }

        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }

        public Integer getTotalMarks() { return totalMarks; }
        public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }

        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }

        public int getCorrect() { return correct; }
        public void setCorrect(int correct) { this.correct = correct; }

        public int getWrong() { return wrong; }
        public void setWrong(int wrong) { this.wrong = wrong; }

        public int getUnanswered() { return unanswered; }
        public void setUnanswered(int unanswered) { this.unanswered = unanswered; }

        public String getTimeTaken() { return timeTaken; }
        public void setTimeTaken(String timeTaken) { this.timeTaken = timeTaken; }

        public String getPerformanceLevel() { return performanceLevel; }
        public void setPerformanceLevel(String performanceLevel) { this.performanceLevel = performanceLevel; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public List<QuestionResultDTO> getQuestionResults() { return questionResults; }
        public void setQuestionResults(List<QuestionResultDTO> questionResults) { this.questionResults = questionResults; }
    }

    public static class QuestionResultDTO {
        private Long questionId;
        private String questionText;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private String selectedAnswer;
        private String correctAnswer;
        private String explanation;
        private boolean isCorrect;
        private Integer marksObtained;
        private Integer maxMarks;

        public QuestionResultDTO(Long questionId, String questionText, String optionA, String optionB, String optionC, String optionD, String selectedAnswer, String correctAnswer, String explanation, boolean isCorrect, Integer marksObtained, Integer maxMarks) {
            this.questionId = questionId;
            this.questionText = questionText;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.selectedAnswer = selectedAnswer;
            this.correctAnswer = correctAnswer;
            this.explanation = explanation;
            this.isCorrect = isCorrect;
            this.marksObtained = marksObtained;
            this.maxMarks = maxMarks;
        }

        public Long getQuestionId() { return questionId; }
        public String getQuestionText() { return questionText; }
        public String getOptionA() { return optionA; }
        public String getOptionB() { return optionB; }
        public String getOptionC() { return optionC; }
        public String getOptionD() { return optionD; }
        public String getSelectedAnswer() { return selectedAnswer; }
        public String getCorrectAnswer() { return correctAnswer; }
        public String getExplanation() { return explanation; }
        public boolean isCorrect() { return isCorrect; }
        public Integer getMarksObtained() { return marksObtained; }
        public Integer getMaxMarks() { return maxMarks; }
    }
}
