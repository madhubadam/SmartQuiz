package com.smartassess.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "assessment_questions")
public class AssessmentQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "question_order")
    private Integer questionOrder;

    @Column(nullable = false)
    private Integer marks;

    public AssessmentQuestion() {}

    public AssessmentQuestion(Assessment assessment, Question question, Integer questionOrder, Integer marks) {
        this.assessment = assessment;
        this.question = question;
        this.questionOrder = questionOrder;
        this.marks = marks;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public Integer getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(Integer questionOrder) { this.questionOrder = questionOrder; }

    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }
}
