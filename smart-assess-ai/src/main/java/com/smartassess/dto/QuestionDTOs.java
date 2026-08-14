package com.smartassess.dto;

import com.smartassess.entity.Question;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class QuestionDTOs {

    public static class QuestionRequest {
        @NotNull(message = "Subject ID is required")
        private Long subjectId;

        private Long topicId;

        @NotBlank(message = "Question text is required")
        private String questionText;

        @NotBlank(message = "Option A is required")
        private String optionA;

        @NotBlank(message = "Option B is required")
        private String optionB;

        @NotBlank(message = "Option C is required")
        private String optionC;

        @NotBlank(message = "Option D is required")
        private String optionD;

        @NotBlank(message = "Correct answer (A, B, C, or D) is required")
        private String correctAnswer;

        private String explanation;

        @NotNull(message = "Difficulty is required")
        private Question.Difficulty difficulty;

        private Question.Source source = Question.Source.MANUAL;
        private boolean approved = true;

        // Getters and Setters
        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

        public Long getTopicId() { return topicId; }
        public void setTopicId(Long topicId) { this.topicId = topicId; }

        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }

        public String getOptionA() { return optionA; }
        public void setOptionA(String optionA) { this.optionA = optionA; }

        public String getOptionB() { return optionB; }
        public void setOptionB(String optionB) { this.optionB = optionB; }

        public String getOptionC() { return optionC; }
        public void setOptionC(String optionC) { this.optionC = optionC; }

        public String getOptionD() { return optionD; }
        public void setOptionD(String optionD) { this.optionD = optionD; }

        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }

        public Question.Difficulty getDifficulty() { return difficulty; }
        public void setDifficulty(Question.Difficulty difficulty) { this.difficulty = difficulty; }

        public Question.Source getSource() { return source; }
        public void setSource(Question.Source source) { this.source = source; }

        public boolean isApproved() { return approved; }
        public void setApproved(boolean approved) { this.approved = approved; }
    }

    public static class AIGenerateRequest {
        @NotNull(message = "Subject ID is required")
        private Long subjectId;

        private Long topicId;

        @NotNull(message = "Difficulty is required")
        private Question.Difficulty difficulty;

        private int count = 5;
        private int marksPerQuestion = 2;

        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

        public Long getTopicId() { return topicId; }
        public void setTopicId(Long topicId) { this.topicId = topicId; }

        public Question.Difficulty getDifficulty() { return difficulty; }
        public void setDifficulty(Question.Difficulty difficulty) { this.difficulty = difficulty; }

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }

        public int getMarksPerQuestion() { return marksPerQuestion; }
        public void setMarksPerQuestion(int marksPerQuestion) { this.marksPerQuestion = marksPerQuestion; }
    }

    public static class AIGenerateResponse {
        private List<Question> questions;
        private String status = "pending_review";
        private String message;

        public AIGenerateResponse(List<Question> questions, String message) {
            this.questions = questions;
            this.message = message;
        }

        public List<Question> getQuestions() { return questions; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
    }
}
