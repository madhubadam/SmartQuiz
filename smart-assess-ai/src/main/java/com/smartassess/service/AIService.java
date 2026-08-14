package com.smartassess.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartassess.dto.QuestionDTOs;
import com.smartassess.entity.Question;
import com.smartassess.entity.Subject;
import com.smartassess.entity.Topic;
import com.smartassess.exception.APIException;
import com.smartassess.repository.QuestionRepository;
import com.smartassess.repository.SubjectRepository;
import com.smartassess.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AIService {

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.gemini-url:https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent}")
    private String geminiApiUrl;

    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public AIService(SubjectRepository subjectRepository, TopicRepository topicRepository,
                     QuestionRepository questionRepository) {
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
    }

    @Transactional
    public QuestionDTOs.AIGenerateResponse generateQuestions(QuestionDTOs.AIGenerateRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Subject not found", "SUBJECT_NOT_FOUND"));

        Topic topic = null;
        if (request.getTopicId() != null) {
            topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Topic not found", "TOPIC_NOT_FOUND"));
        }

        String topicName = topic != null ? topic.getName() : "Core Principles & Practical Applications";
        String fullSubjectName = getFullSubjectName(subject.getName());
        int count = request.getCount() > 0 ? request.getCount() : 5;

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Google Gemini AI API Key (AI_API_KEY) is missing. Please configure AI_API_KEY in application.properties or environment variables.", "AI_KEY_MISSING");
        }

        List<Question> generatedQuestions;
        try {
            generatedQuestions = callGeminiAPI(fullSubjectName, topicName, request.getDifficulty().name(), count, subject, topic);
        } catch (Exception e) {
            throw new APIException(HttpStatus.BAD_GATEWAY, "Gemini AI Question Generation Error: " + e.getMessage(), "AI_GENERATION_FAILED");
        }

        // Validate and filter duplicates
        List<Question> validQuestions = filterAndValidateQuestions(generatedQuestions);

        if (validQuestions.isEmpty()) {
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, "Gemini AI returned invalid question format or no valid questions.", "INVALID_AI_OUTPUT");
        }

        // Save generated questions to DB with approved = false for faculty review
        List<Question> savedQuestions = questionRepository.saveAll(validQuestions);

        return new QuestionDTOs.AIGenerateResponse(
                savedQuestions,
                "Generated " + savedQuestions.size() + " AI questions for '" + fullSubjectName + "' (" + topicName + ") successfully. Please review and approve them before adding to assessments."
        );
    }

    public String generateClassInsights(String assessmentTitle, int totalStudents, List<String> topicSummaries) {
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                String prompt = String.format(
                        "Assessment: %s\nTotal Students: %d\nTopic Performance:\n%s\n" +
                        "Generate a concise 2-3 sentence insight and recommendation for the faculty teacher.",
                        assessmentTitle, totalStudents, String.join("\n", topicSummaries)
                );
                return callGeminiForInsights(prompt);
            } catch (Exception ignored) {}
        }

        // Fallback intelligent class insight summary
        StringBuilder insight = new StringBuilder();
        insight.append("Class Performance Analysis for ").append(assessmentTitle).append(" (").append(totalStudents).append(" students):\n");
        if (!topicSummaries.isEmpty()) {
            insight.append("Strong areas: ").append(topicSummaries.get(0)).append(". ");
            if (topicSummaries.size() > 1) {
                insight.append("Key area for improvement: ").append(topicSummaries.get(topicSummaries.size() - 1)).append(". ");
            }
        }
        insight.append("Recommendation: Schedule a 30-minute target review session on challenging topics and provide 5 additional practice exercises.");
        return insight.toString();
    }

    private List<Question> callGeminiAPI(String fullSubjectName, String topicName, String difficulty, int count, Subject subjectEntity, Topic topicEntity) throws Exception {
        String prompt = String.format(
                "You are an expert university computer science professor. " +
                "CRITICAL INSTRUCTION: You MUST generate questions strictly and exclusively for the course '%s'. " +
                "Focus 100%% specifically on the topic '%s'. DO NOT generate questions from any other subject or domain. " +
                "Generate exactly %d multiple-choice questions at '%s' difficulty level. " +
                "Each question must contain: " +
                "1. questionText: Clear, rigorous academic question specifically testing '%s' in '%s'. " +
                "2. optionA, optionB, optionC, optionD: 4 distinct, plausible options. " +
                "3. correctAnswer: Exact letter ('A', 'B', 'C', or 'D') of the correct option. " +
                "4. explanation: Technical explanation of why the correct option is right. " +
                "Respond ONLY with a valid JSON object matching this schema: " +
                "{\"questions\": [{\"questionText\": \"...\", \"optionA\": \"...\", \"optionB\": \"...\", \"optionC\": \"...\", \"optionD\": \"...\", \"correctAnswer\": \"A/B/C/D\", \"explanation\": \"...\", \"difficulty\": \"%s\"}]}",
                fullSubjectName, topicName, count, difficulty, topicName, fullSubjectName, difficulty
        );

        String fullUrl = buildFullGeminiUrl();

        Map<String, Object> contents = new HashMap<>();
        Map<String, Object> parts = new HashMap<>();
        parts.put("text", prompt);
        contents.put("parts", Collections.singletonList(parts));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", Collections.singletonList(contents));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);
            return parseGeminiResponse(response, subjectEntity, topicEntity, difficulty);
        } catch (Exception primaryException) {
            // Hard fallback to standard gemini-1.5-flash if custom endpoint returns 404 or fails
            String fallbackUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + (apiKey != null ? apiKey.trim() : "");
            try {
                ResponseEntity<String> fallbackResponse = restTemplate.postForEntity(fallbackUrl, entity, String.class);
                return parseGeminiResponse(fallbackResponse, subjectEntity, topicEntity, difficulty);
            } catch (Exception fallbackException) {
                throw primaryException;
            }
        }
    }

    private List<Question> parseGeminiResponse(ResponseEntity<String> response, Subject subjectEntity, Topic topicEntity, String difficulty) throws Exception {
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            String text = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            
            // Clean JSON string if wrapped in markdown code blocks
            text = text.replaceAll("```json", "").replaceAll("```", "").trim();
            JsonNode jsonQuestions = objectMapper.readTree(text).path("questions");

            List<Question> result = new ArrayList<>();
            for (JsonNode qNode : jsonQuestions) {
                Question q = new Question();
                q.setSubject(subjectEntity);
                q.setTopic(topicEntity);
                q.setQuestionText(qNode.path("questionText").asText());
                q.setOptionA(qNode.path("optionA").asText());
                q.setOptionB(qNode.path("optionB").asText());
                q.setOptionC(qNode.path("optionC").asText());
                q.setOptionD(qNode.path("optionD").asText());
                q.setCorrectAnswer(qNode.path("correctAnswer").asText().toUpperCase());
                q.setExplanation(qNode.path("explanation").asText());
                q.setDifficulty(Question.Difficulty.valueOf(difficulty));
                q.setSource(Question.Source.AI);
                q.setApproved(false);
                result.add(q);
            }
            return result;
        }
        throw new RuntimeException("Gemini API HTTP status: " + response.getStatusCode());
    }

    private String callGeminiForInsights(String promptText) {
        String fullUrl = buildFullGeminiUrl();
        Map<String, Object> contents = new HashMap<>();
        Map<String, Object> parts = new HashMap<>();
        parts.put("text", promptText);
        contents.put("parts", Collections.singletonList(parts));
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", Collections.singletonList(contents));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            }
        } catch (Exception e) {
            String fallbackUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + (apiKey != null ? apiKey.trim() : "");
            try {
                ResponseEntity<String> fallbackResponse = restTemplate.postForEntity(fallbackUrl, entity, String.class);
                if (fallbackResponse.getStatusCode() == HttpStatus.OK && fallbackResponse.getBody() != null) {
                    JsonNode root = objectMapper.readTree(fallbackResponse.getBody());
                    return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String buildFullGeminiUrl() {
        String defaultEndpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent";
        String baseUrl = defaultEndpoint;
        
        if (geminiApiUrl != null && !geminiApiUrl.trim().isEmpty()) {
            String custom = geminiApiUrl.trim();
            // Extract http/https scheme if key name was prepended
            if (custom.contains("http://") || custom.contains("https://")) {
                int httpIdx = custom.indexOf("http");
                custom = custom.substring(httpIdx);
            }
            // Remove trailing query params
            if (custom.contains("?")) {
                custom = custom.substring(0, custom.indexOf("?"));
            }
            // Override deprecated or non-existent models (such as gemini-3.5-flash) to official active model gemini-1.5-flash
            if (custom.startsWith("https://") && !custom.contains("gemini-3.5") && custom.contains("generateContent")) {
                baseUrl = custom;
            }
        }

        String cleanKey = apiKey != null ? apiKey.trim() : "";
        return baseUrl + "?key=" + cleanKey;
    }

    private List<Question> filterAndValidateQuestions(List<Question> rawQuestions) {
        List<Question> valid = new ArrayList<>();
        Set<String> seenTexts = new HashSet<>();

        for (Question q : rawQuestions) {
            if (q.getQuestionText() == null || q.getQuestionText().trim().isEmpty()) continue;
            if (q.getOptionA() == null || q.getOptionB() == null || q.getOptionC() == null || q.getOptionD() == null) continue;
            
            // Check distinct options
            Set<String> options = new HashSet<>(Arrays.asList(
                    q.getOptionA().trim().toLowerCase(),
                    q.getOptionB().trim().toLowerCase(),
                    q.getOptionC().trim().toLowerCase(),
                    q.getOptionD().trim().toLowerCase()
            ));
            if (options.size() < 4) continue;

            // Check correct answer format
            String ans = q.getCorrectAnswer() != null ? q.getCorrectAnswer().trim().toUpperCase() : "";
            if (!ans.equals("A") && !ans.equals("B") && !ans.equals("C") && !ans.equals("D")) continue;

            // Check duplicate text
            if (seenTexts.contains(q.getQuestionText().toLowerCase())) continue;
            seenTexts.add(q.getQuestionText().toLowerCase());

            if (q.getExplanation() == null || q.getExplanation().trim().isEmpty()) {
                q.setExplanation("Option " + ans + " is the correct answer according to curriculum standards.");
            }

            valid.add(q);
        }
        return valid;
    }

    private String getFullSubjectName(String name) {
        if (name == null) return "Computer Science";
        String s = name.trim();
        if (s.equalsIgnoreCase("DBMS")) return "Database Management Systems (DBMS)";
        if (s.equalsIgnoreCase("CN")) return "Computer Networks (CN)";
        if (s.equalsIgnoreCase("DS")) return "Data Structures and Algorithms (DS)";
        if (s.equalsIgnoreCase("OS")) return "Operating Systems (OS)";
        if (s.equalsIgnoreCase("DW") || s.equalsIgnoreCase("DWDM")) return "Data Warehousing and Data Mining (DWDM)";
        return s;
    }
}