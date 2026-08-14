package com.smartassess.repository;

import com.smartassess.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findBySubjectId(Long subjectId);
    List<Question> findBySubjectIdAndApprovedTrue(Long subjectId);
    List<Question> findByTopicId(Long topicId);

    @Query("SELECT q FROM Question q WHERE " +
           "(:subjectId IS NULL OR q.subject.id = :subjectId) AND " +
           "(:topicId IS NULL OR q.topic.id = :topicId) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty) AND " +
           "(:source IS NULL OR q.source = :source) AND " +
           "(:query IS NULL OR LOWER(q.questionText) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Question> searchQuestions(@Param("subjectId") Long subjectId,
                                   @Param("topicId") Long topicId,
                                   @Param("difficulty") Question.Difficulty difficulty,
                                   @Param("source") Question.Source source,
                                   @Param("query") String query);
}
