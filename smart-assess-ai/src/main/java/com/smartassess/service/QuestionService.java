package com.smartassess.service;

import com.smartassess.dto.QuestionDTOs;
import com.smartassess.entity.Question;
import com.smartassess.entity.Subject;
import com.smartassess.entity.Topic;
import com.smartassess.exception.APIException;
import com.smartassess.exception.ResourceNotFoundException;
import com.smartassess.repository.QuestionRepository;
import com.smartassess.repository.SubjectRepository;
import com.smartassess.repository.TopicRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;

    public QuestionService(QuestionRepository questionRepository, SubjectRepository subjectRepository, TopicRepository topicRepository) {
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
    }

    public List<Question> searchQuestions(Long subjectId, Long topicId, Question.Difficulty difficulty, Question.Source source, String query) {
        return questionRepository.searchQuestions(subjectId, topicId, difficulty, source, query);
    }

    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + id));
    }

    @Transactional
    public Question createQuestion(QuestionDTOs.QuestionRequest request) {
        validateQuestionRequest(request);

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id " + request.getSubjectId()));

        Topic topic = null;
        if (request.getTopicId() != null) {
            topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id " + request.getTopicId()));
        }

        Question question = new Question();
        question.setSubject(subject);
        question.setTopic(topic);
        question.setQuestionText(request.getQuestionText().trim());
        question.setOptionA(request.getOptionA().trim());
        question.setOptionB(request.getOptionB().trim());
        question.setOptionC(request.getOptionC().trim());
        question.setOptionD(request.getOptionD().trim());
        question.setCorrectAnswer(request.getCorrectAnswer().toUpperCase().trim());
        question.setExplanation(request.getExplanation());
        question.setDifficulty(request.getDifficulty());
        question.setSource(request.getSource() != null ? request.getSource() : Question.Source.MANUAL);
        question.setApproved(request.isApproved());

        return questionRepository.save(question);
    }

    @Transactional
    public Question updateQuestion(Long id, QuestionDTOs.QuestionRequest request) {
        validateQuestionRequest(request);
        Question question = getQuestionById(id);

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id " + request.getSubjectId()));

        Topic topic = null;
        if (request.getTopicId() != null) {
            topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id " + request.getTopicId()));
        }

        question.setSubject(subject);
        question.setTopic(topic);
        question.setQuestionText(request.getQuestionText().trim());
        question.setOptionA(request.getOptionA().trim());
        question.setOptionB(request.getOptionB().trim());
        question.setOptionC(request.getOptionC().trim());
        question.setOptionD(request.getOptionD().trim());
        question.setCorrectAnswer(request.getCorrectAnswer().toUpperCase().trim());
        question.setExplanation(request.getExplanation());
        question.setDifficulty(request.getDifficulty());
        question.setApproved(request.isApproved());

        return questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        Question question = getQuestionById(id);
        questionRepository.delete(question);
    }

    private void validateQuestionRequest(QuestionDTOs.QuestionRequest request) {
        if (request.getQuestionText() == null || request.getQuestionText().trim().isEmpty()) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Question text cannot be empty", "INVALID_QUESTION");
        }
        if (request.getOptionA() == null || request.getOptionB() == null || request.getOptionC() == null || request.getOptionD() == null) {
            throw new APIException(HttpStatus.BAD_REQUEST, "All four options (A, B, C, D) are required", "INVALID_OPTIONS");
        }
        String a = request.getOptionA().trim(), b = request.getOptionB().trim(), c = request.getOptionC().trim(), d = request.getOptionD().trim();
        if (a.equalsIgnoreCase(b) || a.equalsIgnoreCase(c) || a.equalsIgnoreCase(d) || b.equalsIgnoreCase(c) || b.equalsIgnoreCase(d) || c.equalsIgnoreCase(d)) {
            throw new APIException(HttpStatus.BAD_REQUEST, "All four options must be distinct", "DUPLICATE_OPTIONS");
        }
        String ca = request.getCorrectAnswer();
        if (ca == null || (!ca.equalsIgnoreCase("A") && !ca.equalsIgnoreCase("B") && !ca.equalsIgnoreCase("C") && !ca.equalsIgnoreCase("D"))) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Correct answer must be A, B, C, or D", "INVALID_CORRECT_ANSWER");
        }
    }
}
