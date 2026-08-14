package com.smartassess.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student_answers")
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private Attempt attempt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "selected_answer", length = 5)
    private String selectedAnswer;

    @Column(nullable = false)
    private boolean correct;

    @Column(name = "marks_obtained", nullable = false)
    private Integer marksObtained;

    public StudentAnswer() {}

    public StudentAnswer(Attempt attempt, Question question, String selectedAnswer, boolean correct, Integer marksObtained) {
        this.attempt = attempt;
        this.question = question;
        this.selectedAnswer = selectedAnswer;
        this.correct = correct;
        this.marksObtained = marksObtained;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Attempt getAttempt() { return attempt; }
    public void setAttempt(Attempt attempt) { this.attempt = attempt; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public String getSelectedAnswer() { return selectedAnswer; }
    public void setSelectedAnswer(String selectedAnswer) { this.selectedAnswer = selectedAnswer; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public Integer getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Integer marksObtained) { this.marksObtained = marksObtained; }
}
