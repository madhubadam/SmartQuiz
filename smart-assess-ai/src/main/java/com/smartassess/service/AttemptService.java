package com.smartassess.service;

import com.smartassess.dto.AttemptDTOs;
import com.smartassess.entity.*;
import com.smartassess.exception.APIException;
import com.smartassess.exception.ResourceNotFoundException;
import com.smartassess.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttemptService {

    private final AttemptRepository attemptRepository;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final UserRepository userRepository;

    public AttemptService(AttemptRepository attemptRepository,
                          AssessmentRepository assessmentRepository,
                          AssessmentQuestionRepository assessmentQuestionRepository,
                          StudentAnswerRepository studentAnswerRepository,
                          UserRepository userRepository) {
        this.attemptRepository = attemptRepository;
        this.assessmentRepository = assessmentRepository;
        this.assessmentQuestionRepository = assessmentQuestionRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AttemptDTOs.StartAttemptResponse startAttempt(Long assessmentId, Long studentUserId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));

        if (assessment.getStatus() != Assessment.Status.PUBLISHED) {
            throw new APIException(HttpStatus.BAD_REQUEST, "This assessment is not currently active or published", "TEST_NOT_PUBLISHED");
        }

        User student = userRepository.findById(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student user not found"));

        // Check if student already has a completed attempt
        boolean completed = attemptRepository.existsByAssessmentIdAndStudentIdAndStatus(
                assessmentId, studentUserId, Attempt.Status.COMPLETED);
        if (completed) {
            throw new APIException(HttpStatus.CONFLICT, "You have already completed this assessment", "DUPLICATE_SUBMISSION");
        }

        // Check for existing in-progress attempt
        Attempt attempt = attemptRepository.findByAssessmentIdAndStudentIdAndStatus(
                assessmentId, studentUserId, Attempt.Status.IN_PROGRESS)
                .orElseGet(() -> {
                    Attempt newAttempt = new Attempt();
                    newAttempt.setAssessment(assessment);
                    newAttempt.setStudent(student);
                    newAttempt.setStartedAt(LocalDateTime.now());
                    newAttempt.setStatus(Attempt.Status.IN_PROGRESS);
                    return attemptRepository.save(newAttempt);
                });

        // Always retrieve questions in strict defined order for consistency
        List<AssessmentQuestion> aqList = assessmentQuestionRepository.findByAssessmentIdOrderByQuestionOrderAsc(assessmentId);

        // Map client questions preserving original option keys (A=OptionA, B=OptionB, C=OptionC, D=OptionD)
        List<AttemptDTOs.ClientQuestionDTO> clientQuestions = aqList.stream().map(aq -> {
            Question q = aq.getQuestion();
            return new AttemptDTOs.ClientQuestionDTO(
                    q.getId(),
                    q.getQuestionText(),
                    q.getOptionA(),
                    q.getOptionB(),
                    q.getOptionC(),
                    q.getOptionD(),
                    aq.getMarks()
            );
        }).collect(Collectors.toList());

        int durationSeconds = assessment.getDurationMinutes() * 60;

        return new AttemptDTOs.StartAttemptResponse(
                attempt.getId(),
                assessment.getTitle(),
                assessment.getSubject() != null ? assessment.getSubject().getName() : "",
                durationSeconds,
                clientQuestions
        );
    }

    @Transactional
    public AttemptDTOs.AttemptResultResponse submitAttempt(Long attemptId, AttemptDTOs.SubmitAttemptRequest submitRequest, Long studentUserId) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

        if (!attempt.getStudent().getId().equals(studentUserId)) {
            throw new APIException(HttpStatus.FORBIDDEN, "Unauthorized submission for another student", "UNAUTHORIZED_ATTEMPT");
        }

        if (attempt.getStatus() == Attempt.Status.COMPLETED) {
            throw new APIException(HttpStatus.CONFLICT, "Assessment has already been submitted", "DUPLICATE_SUBMISSION");
        }

        Assessment assessment = attempt.getAssessment();
        LocalDateTime now = LocalDateTime.now();
        attempt.setSubmittedAt(now);
        attempt.setStatus(Attempt.Status.COMPLETED);

        // Map submitted answers by question ID
        Map<Long, String> userAnswers = new HashMap<>();
        if (submitRequest.getAnswers() != null) {
            for (AttemptDTOs.SubmitAnswerDTO ans : submitRequest.getAnswers()) {
                if (ans.getQuestionId() != null && ans.getSelectedAnswer() != null) {
                    userAnswers.put(ans.getQuestionId(), ans.getSelectedAnswer().trim().toUpperCase());
                }
            }
        }

        List<AssessmentQuestion> aqList = assessmentQuestionRepository.findByAssessmentIdOrderByQuestionOrderAsc(assessment.getId());

        int calculatedScore = 0;
        int totalMarks = 0;
        int correctCount = 0;
        int wrongCount = 0;
        int unansweredCount = 0;

        List<StudentAnswer> studentAnswersToSave = new ArrayList<>();
        List<AttemptDTOs.QuestionResultDTO> questionResultDTOs = new ArrayList<>();

        for (AssessmentQuestion aq : aqList) {
            Question q = aq.getQuestion();
            int questionMarks = aq.getMarks();
            totalMarks += questionMarks;

            String selected = userAnswers.get(q.getId());
            boolean isCorrect = false;
            int marksObtained = 0;

            if (selected == null || selected.trim().isEmpty() || selected.equalsIgnoreCase("null")) {
                unansweredCount++;
                selected = null;
            } else {
                if (selected.equalsIgnoreCase(q.getCorrectAnswer())) {
                    isCorrect = true;
                    marksObtained = questionMarks;
                    calculatedScore += questionMarks;
                    correctCount++;
                } else {
                    wrongCount++;
                }
            }

            StudentAnswer sa = new StudentAnswer(attempt, q, selected, isCorrect, marksObtained);
            studentAnswersToSave.add(sa);

            questionResultDTOs.add(new AttemptDTOs.QuestionResultDTO(
                    q.getId(),
                    q.getQuestionText(),
                    q.getOptionA(),
                    q.getOptionB(),
                    q.getOptionC(),
                    q.getOptionD(),
                    selected,
                    q.getCorrectAnswer(),
                    q.getExplanation(),
                    isCorrect,
                    marksObtained,
                    questionMarks
            ));
        }

        studentAnswerRepository.saveAll(studentAnswersToSave);

        double percentage = totalMarks > 0 ? ((double) calculatedScore / totalMarks) * 100.0 : 0.0;
        percentage = Math.round(percentage * 10.0) / 10.0;

        attempt.setScore(calculatedScore);
        attempt.setTotalMarks(totalMarks);
        attempt.setPercentage(percentage);
        attemptRepository.save(attempt);

        Duration duration = Duration.between(attempt.getStartedAt(), now);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        String timeTakenStr = minutes + " minutes " + seconds + " seconds";

        String performanceLevel = getPerformanceLevel(percentage);

        AttemptDTOs.AttemptResultResponse response = new AttemptDTOs.AttemptResultResponse();
        response.setAttemptId(attempt.getId());
        response.setScore(calculatedScore);
        response.setTotalMarks(totalMarks);
        response.setPercentage(percentage);
        response.setCorrect(correctCount);
        response.setWrong(wrongCount);
        response.setUnanswered(unansweredCount);
        response.setTimeTaken(timeTakenStr);
        response.setPerformanceLevel(performanceLevel);
        response.setMessage("Successfully submitted");
        response.setQuestionResults(questionResultDTOs);

        return response;
    }

    public AttemptDTOs.AttemptResultResponse getAttemptResult(Long attemptId, Long studentUserId, boolean isFaculty) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

        if (!isFaculty && !attempt.getStudent().getId().equals(studentUserId)) {
            throw new APIException(HttpStatus.FORBIDDEN, "Access denied to this attempt result", "ACCESS_DENIED");
        }

        List<StudentAnswer> answers = studentAnswerRepository.findByAttemptId(attemptId);
        List<AssessmentQuestion> aqList = assessmentQuestionRepository.findByAssessmentIdOrderByQuestionOrderAsc(attempt.getAssessment().getId());
        Map<Long, Integer> marksMap = aqList.stream().collect(Collectors.toMap(aq -> aq.getQuestion().getId(), AssessmentQuestion::getMarks));

        int correctCount = 0;
        int wrongCount = 0;
        int unansweredCount = 0;

        List<AttemptDTOs.QuestionResultDTO> questionResults = new ArrayList<>();
        for (StudentAnswer sa : answers) {
            Question q = sa.getQuestion();
            if (sa.getSelectedAnswer() == null) {
                unansweredCount++;
            } else if (sa.isCorrect()) {
                correctCount++;
            } else {
                wrongCount++;
            }
            int maxMarks = marksMap.getOrDefault(q.getId(), 1);
            questionResults.add(new AttemptDTOs.QuestionResultDTO(
                    q.getId(),
                    q.getQuestionText(),
                    q.getOptionA(),
                    q.getOptionB(),
                    q.getOptionC(),
                    q.getOptionD(),
                    sa.getSelectedAnswer(),
                    q.getCorrectAnswer(),
                    q.getExplanation(),
                    sa.isCorrect(),
                    sa.getMarksObtained(),
                    maxMarks
            ));
        }

        LocalDateTime end = attempt.getSubmittedAt() != null ? attempt.getSubmittedAt() : LocalDateTime.now();
        Duration duration = Duration.between(attempt.getStartedAt(), end);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        String timeTakenStr = minutes + " minutes " + seconds + " seconds";

        AttemptDTOs.AttemptResultResponse response = new AttemptDTOs.AttemptResultResponse();
        response.setAttemptId(attempt.getId());
        response.setScore(attempt.getScore());
        response.setTotalMarks(attempt.getTotalMarks());
        response.setPercentage(attempt.getPercentage());
        response.setCorrect(correctCount);
        response.setWrong(wrongCount);
        response.setUnanswered(unansweredCount);
        response.setTimeTaken(timeTakenStr);
        response.setPerformanceLevel(getPerformanceLevel(attempt.getPercentage()));
        response.setMessage("Assessment Result");
        response.setQuestionResults(questionResults);

        return response;
    }

    public List<Attempt> getStudentAttempts(Long studentUserId) {
        return attemptRepository.findByStudentId(studentUserId);
    }

    private String getPerformanceLevel(double percentage) {
        if (percentage >= 85.0) return "Excellent";
        if (percentage >= 70.0) return "Good";
        if (percentage >= 50.0) return "Average";
        return "Poor";
    }
}