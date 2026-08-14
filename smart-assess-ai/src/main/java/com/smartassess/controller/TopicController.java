package com.smartassess.controller;

import com.smartassess.entity.Topic;
import com.smartassess.service.SubjectTopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final SubjectTopicService subjectTopicService;

    public TopicController(SubjectTopicService subjectTopicService) {
        this.subjectTopicService = subjectTopicService;
    }

    @GetMapping
    public ResponseEntity<List<Topic>> getTopicsBySubject(@RequestParam(required = false) Long subjectId) {
        if (subjectId != null) {
            return ResponseEntity.ok(subjectTopicService.getTopicsBySubject(subjectId));
        }
        return ResponseEntity.ok(Collections.emptyList());
    }

    @PostMapping
    public ResponseEntity<Topic> createTopic(@RequestBody Map<String, Object> body) {
        Long subjectId = Long.parseLong(body.get("subjectId").toString());
        String name = body.get("name").toString();
        return ResponseEntity.ok(subjectTopicService.createTopic(subjectId, name));
    }
}
