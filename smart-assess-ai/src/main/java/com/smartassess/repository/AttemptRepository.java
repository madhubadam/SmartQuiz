package com.smartassess.repository;

import com.smartassess.entity.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findByStudentId(Long studentId);
    List<Attempt> findByAssessmentId(Long assessmentId);
    Optional<Attempt> findByAssessmentIdAndStudentIdAndStatus(Long assessmentId, Long studentId, Attempt.Status status);
    boolean existsByAssessmentIdAndStudentIdAndStatus(Long assessmentId, Long studentId, Attempt.Status status);
}
