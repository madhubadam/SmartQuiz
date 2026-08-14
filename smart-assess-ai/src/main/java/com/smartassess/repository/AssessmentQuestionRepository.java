package com.smartassess.repository;

import com.smartassess.entity.AssessmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Long> {
    List<AssessmentQuestion> findByAssessmentIdOrderByQuestionOrderAsc(Long assessmentId);
    void deleteByAssessmentId(Long assessmentId);
}
