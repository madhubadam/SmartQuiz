package com.smartassess.repository;

import com.smartassess.entity.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    List<StudentAnswer> findByAttemptId(Long attemptId);
    List<StudentAnswer> findByQuestionId(Long questionId);
}
