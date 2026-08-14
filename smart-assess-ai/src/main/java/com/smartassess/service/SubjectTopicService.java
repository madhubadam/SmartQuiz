package com.smartassess.service;

import com.smartassess.entity.Subject;
import com.smartassess.entity.Topic;
import com.smartassess.exception.ResourceNotFoundException;
import com.smartassess.repository.SubjectRepository;
import com.smartassess.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectTopicService {

    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;

    public SubjectTopicService(SubjectRepository subjectRepository, TopicRepository topicRepository) {
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Subject getSubjectById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id " + id));
    }

    public Subject createSubject(Subject subject, Long facultyId) {
        subject.setCreatedBy(facultyId);
        return subjectRepository.save(subject);
    }

    public List<Topic> getTopicsBySubject(Long subjectId) {
        return topicRepository.findBySubjectId(subjectId);
    }

    public Topic createTopic(Long subjectId, String name) {
        Subject subject = getSubjectById(subjectId);
        Topic topic = new Topic(name, subject);
        return topicRepository.save(topic);
    }
}
