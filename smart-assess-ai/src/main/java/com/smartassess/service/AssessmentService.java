package com.smartassess.service;

import com.smartassess.dto.AssessmentDTOs;
import com.smartassess.entity.*;
import com.smartassess.exception.APIException;
import com.smartassess.exception.ResourceNotFoundException;
import com.smartassess.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssessmentService {

    private static final String CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AssessmentRepository assessmentRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public AssessmentService(AssessmentRepository assessmentRepository,
                             AssessmentQuestionRepository assessmentQuestionRepository,
                             QuestionRepository questionRepository,
                             SubjectRepository subjectRepository,
                             UserRepository userRepository) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentQuestionRepository = assessmentQuestionRepository;
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    public List<Assessment> getAllAssessments() {
        return assessmentRepository.findAll();
    }

    public List<Assessment> getFacultyAssessments(Long facultyId) {
        return assessmentRepository.findByCreatedById(facultyId);
    }

    public List<Assessment> getPublishedAssessments() {
        return assessmentRepository.findByStatus(Assessment.Status.PUBLISHED);
    }

    public Assessment getAssessmentById(Long id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id " + id));
    }

    public Assessment getAssessmentByShareToken(String shareToken) {
        return assessmentRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new ResourceNotFoundException("Test link is invalid or has expired (" + shareToken + ")"));
    }

    public List<AssessmentQuestion> getAssessmentQuestions(Long assessmentId) {
        return assessmentQuestionRepository.findByAssessmentIdOrderByQuestionOrderAsc(assessmentId);
    }

    @Transactional
    public Assessment createAssessment(AssessmentDTOs.AssessmentRequest request, Long facultyUserId) {
        User faculty = userRepository.findById(facultyUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty user not found"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        Assessment assessment = new Assessment();
        assessment.setTitle(request.getTitle());
        assessment.setDescription(request.getDescription());
        assessment.setSubject(subject);
        assessment.setDurationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 30);
        assessment.setTotalMarks(request.getTotalMarks() != null ? request.getTotalMarks() : 100);
        assessment.setShuffleQuestions(request.isShuffleQuestions());
        assessment.setShuffleOptions(request.isShuffleOptions());
        assessment.setStatus(Assessment.Status.DRAFT);
        assessment.setCreatedBy(faculty);

        Assessment saved = assessmentRepository.save(assessment);

        // Attach questions
        if (request.getQuestionItems() != null && !request.getQuestionItems().isEmpty()) {
            int order = 1;
            int computedTotalMarks = 0;
            for (AssessmentDTOs.QuestionMarkItem item : request.getQuestionItems()) {
                Question question = questionRepository.findById(item.getQuestionId())
                        .orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + item.getQuestionId()));

                int marks = item.getMarks() != null && item.getMarks() > 0 ? item.getMarks() : 1;
                computedTotalMarks += marks;

                AssessmentQuestion aq = new AssessmentQuestion(saved, question, order++, marks);
                assessmentQuestionRepository.save(aq);
            }
            saved.setTotalMarks(computedTotalMarks);
            saved = assessmentRepository.save(saved);
        }

        return saved;
    }

    @Transactional
    public Assessment publishAssessment(Long id) {
        Assessment assessment = getAssessmentById(id);

        if (assessment.getStatus() == Assessment.Status.PUBLISHED) {
            return assessment; // already published
        }

        List<AssessmentQuestion> questions = assessmentQuestionRepository.findByAssessmentIdOrderByQuestionOrderAsc(id);
        if (questions.isEmpty()) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Cannot publish an assessment with zero questions", "NO_QUESTIONS");
        }

        assessment.setStatus(Assessment.Status.PUBLISHED);
        assessment.setPublishedAt(LocalDateTime.now());
        
        if (assessment.getShareToken() == null) {
            assessment.setShareToken(generateShareToken());
        }

        return assessmentRepository.save(assessment);
    }

    @Transactional
    public Assessment closeAssessment(Long id) {
        Assessment assessment = getAssessmentById(id);
        assessment.setStatus(Assessment.Status.CLOSED);
        return assessmentRepository.save(assessment);
    }

    @Transactional
    public void deleteAssessment(Long id) {
        Assessment assessment = getAssessmentById(id);
        assessmentQuestionRepository.deleteByAssessmentId(id);
        assessmentRepository.delete(assessment);
    }

    private String generateShareToken() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
