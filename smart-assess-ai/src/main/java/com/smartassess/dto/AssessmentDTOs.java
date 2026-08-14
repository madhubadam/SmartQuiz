package com.smartassess.dto;

import com.smartassess.entity.Assessment;

import java.util.List;

public class AssessmentDTOs {

    public static class AssessmentRequest {
        private String title;
        private String description;
        private Long subjectId;
        private Integer durationMinutes;
        private Integer totalMarks;
        private boolean shuffleQuestions;
        private boolean shuffleOptions;
        private List<QuestionMarkItem> questionItems;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

        public Integer getTotalMarks() { return totalMarks; }
        public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }

        public boolean isShuffleQuestions() { return shuffleQuestions; }
        public void setShuffleQuestions(boolean shuffleQuestions) { this.shuffleQuestions = shuffleQuestions; }

        public boolean isShuffleOptions() { return shuffleOptions; }
        public void setShuffleOptions(boolean shuffleOptions) { this.shuffleOptions = shuffleOptions; }

        public List<QuestionMarkItem> getQuestionItems() { return questionItems; }
        public void setQuestionItems(List<QuestionMarkItem> questionItems) { this.questionItems = questionItems; }
    }

    public static class QuestionMarkItem {
        private Long questionId;
        private Integer marks;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }

        public Integer getMarks() { return marks; }
        public void setMarks(Integer marks) { this.marks = marks; }
    }

    public static class PublicTestDTO {
        private Long id;
        private String title;
        private String description;
        private String subjectName;
        private String facultyName;
        private Integer durationMinutes;
        private Integer questionCount;
        private Integer totalMarks;
        private String status;
        private String shareToken;

        public PublicTestDTO(Assessment assessment, int questionCount) {
            this.id = assessment.getId();
            this.title = assessment.getTitle();
            this.description = assessment.getDescription();
            this.subjectName = assessment.getSubject() != null ? assessment.getSubject().getName() : "";
            this.facultyName = assessment.getCreatedBy() != null ? assessment.getCreatedBy().getName() : "";
            this.durationMinutes = assessment.getDurationMinutes();
            this.questionCount = questionCount;
            this.totalMarks = assessment.getTotalMarks();
            this.status = assessment.getStatus().name();
            this.shareToken = assessment.getShareToken();
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getSubjectName() { return subjectName; }
        public String getFacultyName() { return facultyName; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public Integer getQuestionCount() { return questionCount; }
        public Integer getTotalMarks() { return totalMarks; }
        public String getStatus() { return status; }
        public String getShareToken() { return shareToken; }
    }
}
