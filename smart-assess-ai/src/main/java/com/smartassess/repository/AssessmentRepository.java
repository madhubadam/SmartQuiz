package com.smartassess.repository;

import com.smartassess.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    Optional<Assessment> findByShareToken(String shareToken);
    List<Assessment> findByCreatedById(Long userId);
    List<Assessment> findByStatus(Assessment.Status status);
}
