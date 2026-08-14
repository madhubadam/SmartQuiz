package com.smartassess.config;

import com.smartassess.entity.*;
import com.smartassess.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final String CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           SubjectRepository subjectRepository,
                           TopicRepository topicRepository,
                           QuestionRepository questionRepository,
                           AssessmentRepository assessmentRepository,
                           AssessmentQuestionRepository assessmentQuestionRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
        this.assessmentRepository = assessmentRepository;
        this.assessmentQuestionRepository = assessmentQuestionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Ensure Demo Faculty and Student Accounts exist
        User faculty = userRepository.findByEmail("faculty@example.com").orElseGet(() -> {
            User f = new User("Dr. Sarah Jenkins", "faculty@example.com", passwordEncoder.encode("Faculty@123"), User.Role.FACULTY, "FAC-8890");
            return userRepository.save(f);
        });

        userRepository.findByEmail("student@example.com").orElseGet(() -> {
            User s = new User("Alex Rivera", "student@example.com", passwordEncoder.encode("Student@123"), User.Role.STUDENT, "STU-2024");
            return userRepository.save(s);
        });

        // 2. Self-healing Subject & Topic Seeding for all subjects including aliases (DBMS, CN, OS, DS, DWDM)
        initAllSubjectsAndTopics(faculty);

        // 3. Ensure Sample Demo Assessment exists with shareToken "8F7K2P"
        initDemoAssessment(faculty);

        // 4. Safe migration: ensure any published assessment in DB has a unique shareToken
        migrateExistingAssessments();
    }

    private void initAllSubjectsAndTopics(User faculty) {
        Map<String, List<String>> subjectTopicsMap = new LinkedHashMap<>();

        subjectTopicsMap.put("Database Management Systems", Arrays.asList(
                "DBMS Fundamentals", "ER Model", "Relational Model", "SQL", "Joins",
                "Normalization", "Functional Dependencies", "Transactions", "Concurrency Control",
                "Indexing", "Query Processing", "Database Security"
        ));

        subjectTopicsMap.put("Computer Networks", Arrays.asList(
                "Network Fundamentals", "OSI Model", "TCP/IP Model", "Physical Layer",
                "Data Link Layer", "Network Layer", "Transport Layer", "Application Layer",
                "IP Addressing", "Subnetting", "Routing", "TCP", "UDP", "HTTP/HTTPS", "DNS", "DHCP", "Network Security"
        ));

        subjectTopicsMap.put("Data Structures", Arrays.asList(
                "Arrays", "Linked Lists", "Stacks", "Queues", "Trees",
                "Binary Search Trees", "Heaps", "Hashing", "Graphs",
                "Sorting Algorithms", "Searching Algorithms", "Recursion", "Dynamic Programming"
        ));

        subjectTopicsMap.put("Data Warehousing and Data Mining", Arrays.asList(
                "Data Warehousing", "Data Warehouse Architecture", "ETL", "Data Preprocessing",
                "Data Integration", "Data Reduction", "OLAP", "OLTP", "Star Schema",
                "Snowflake Schema", "Association Rule Mining", "Apriori", "FP-Growth",
                "Classification", "Decision Trees", "J48", "ID3", "Clustering", "K-Means", "Weka"
        ));

        subjectTopicsMap.put("Operating Systems", Arrays.asList(
                "OS Fundamentals", "Processes", "Threads", "CPU Scheduling",
                "Process Synchronization", "Deadlocks", "Memory Management", "Virtual Memory",
                "Paging", "Page Replacement", "File Systems", "Disk Scheduling", "I/O Management", "Protection and Security"
        ));

        subjectTopicsMap.put("Java Programming", Arrays.asList(
                "Java Fundamentals", "Variables and Data Types", "Operators", "Conditional Statements",
                "Loops", "Arrays", "Strings", "Methods", "Classes and Objects", "Constructors",
                "Inheritance", "Polymorphism", "Encapsulation", "Abstraction", "Interfaces",
                "Exception Handling", "Collections", "ArrayList", "HashMap", "HashSet", "Multithreading", "JDBC"
        ));

        subjectTopicsMap.put("Formal Languages and Automata Theory", Arrays.asList(
                "Alphabets and Strings", "Regular Languages", "Regular Expressions", "Finite Automata",
                "DFA", "NFA", "DFA Minimization", "Regular Grammars", "Context Free Grammars",
                "Parse Trees", "Pushdown Automata", "Context Free Languages", "Turing Machines", "Decidability", "Chomsky Hierarchy"
        ));

        subjectTopicsMap.put("Software Engineering", Arrays.asList(
                "Software Development Life Cycle", "Requirements Engineering", "Software Design",
                "UML", "Agile", "Scrum", "Testing", "Unit Testing", "Integration Testing",
                "System Testing", "Software Maintenance", "Software Project Management", "Risk Management", "Software Quality"
        ));

        subjectTopicsMap.put("Web Development", Arrays.asList(
                "HTML", "CSS", "JavaScript", "DOM", "Events", "Forms", "HTTP", "REST APIs",
                "JSON", "Authentication", "Web Security", "Responsive Design", "Frontend Architecture", "Backend Fundamentals"
        ));

        subjectTopicsMap.put("Python Programming", Arrays.asList(
                "Python Fundamentals", "Variables and Data Types", "Operators", "Conditions",
                "Loops", "Functions", "Lists", "Tuples", "Sets", "Dictionaries", "Strings",
                "File Handling", "Exception Handling", "OOP", "Modules", "NumPy", "Pandas"
        ));

        // Define Aliases to support legacy database records like "DBMS", "CN", "DS", "OS", "DW"
        Map<String, String[]> aliasesMap = new HashMap<>();
        aliasesMap.put("Database Management Systems", new String[]{"DBMS", "Database Management Systems", "Database"});
        aliasesMap.put("Computer Networks", new String[]{"Computer Networks", "CN"});
        aliasesMap.put("Data Structures", new String[]{"Data Structures", "DS"});
        aliasesMap.put("Operating Systems", new String[]{"Operating Systems", "OS"});
        aliasesMap.put("Data Warehousing and Data Mining", new String[]{"Data Warehousing and Data Mining", "Data Warehousing", "DW", "DWDM"});

        List<Subject> existingSubjectsInDb = subjectRepository.findAll();

        for (Map.Entry<String, List<String>> entry : subjectTopicsMap.entrySet()) {
            String primarySubjectName = entry.getKey();
            List<String> topicsList = entry.getValue();
            String[] aliases = aliasesMap.getOrDefault(primarySubjectName, new String[]{primarySubjectName});

            // Find all matching Subject entities in DB (primary or alias match)
            Set<Subject> targetSubjects = new HashSet<>();
            for (Subject s : existingSubjectsInDb) {
                for (String alias : aliases) {
                    if (s.getName().equalsIgnoreCase(alias) || s.getName().toLowerCase().contains(alias.toLowerCase())) {
                        targetSubjects.add(s);
                    }
                }
            }

            // If no existing subject matches, create the primary subject
            if (targetSubjects.isEmpty()) {
                Subject newSub = subjectRepository.save(new Subject(primarySubjectName, primarySubjectName + " Curriculum", faculty.getId()));
                targetSubjects.add(newSub);
            }

            // Populate topics for ALL matching target subjects in database
            for (Subject sub : targetSubjects) {
                List<Topic> existingTopics = topicRepository.findBySubjectId(sub.getId());
                Set<String> existingNames = new HashSet<>();
                for (Topic t : existingTopics) existingNames.add(t.getName().toLowerCase());

                for (String topicName : topicsList) {
                    if (!existingNames.contains(topicName.toLowerCase())) {
                        topicRepository.save(new Topic(topicName, sub));
                    }
                }
            }
        }
    }

    private void initDemoAssessment(User faculty) {
        if (assessmentRepository.findByShareToken("8F7K2P").isPresent()) {
            return;
        }

        Subject dw = subjectRepository.findAll().stream()
                .filter(s -> s.getName().toLowerCase().contains("warehous") || s.getName().equalsIgnoreCase("DW") || s.getName().equalsIgnoreCase("DWDM"))
                .findFirst().orElse(null);

        if (dw == null) return;

        List<Topic> dwTopics = topicRepository.findBySubjectId(dw.getId());
        Topic olapTopic = dwTopics.stream().filter(t -> t.getName().equalsIgnoreCase("OLAP") || t.getName().toLowerCase().contains("olap")).findFirst().orElse(dwTopics.isEmpty() ? null : dwTopics.get(0));
        Topic etlTopic = dwTopics.stream().filter(t -> t.getName().equalsIgnoreCase("ETL") || t.getName().toLowerCase().contains("etl")).findFirst().orElse(olapTopic);

        Question q1 = createQuestion(dw, olapTopic,
                "What OLAP operation aggregates data by climbing up a concept hierarchy for a dimension?",
                "Roll-up", "Drill-down", "Slice", "Dice",
                "A", "Roll-up aggregates data by climbing up a concept hierarchy or by dimension reduction.",
                Question.Difficulty.MEDIUM, Question.Source.AI, true);

        Question q2 = createQuestion(dw, olapTopic,
                "Which OLAP operation navigates from less detailed data to more detailed data?",
                "Roll-up", "Drill-down", "Pivot", "Slice",
                "B", "Drill-down reverses roll-up by stepping down concept hierarchies to finer data levels.",
                Question.Difficulty.EASY, Question.Source.AI, true);

        Question q3 = createQuestion(dw, etlTopic,
                "In Data Warehousing ETL architecture, what does the 'E' stand for?",
                "Evaluation", "Extraction", "Execution", "Enumeration",
                "B", "ETL stands for Extract, Transform, and Load. 'E' stands for Extraction.",
                Question.Difficulty.EASY, Question.Source.MANUAL, true);

        Question q4 = createQuestion(dw, etlTopic,
                "Which phase of ETL handles data validation, deduplication, and normalization?",
                "Extract", "Transform", "Load", "Staging",
                "B", "Transformation cleanses, normalizes, and validates raw data according to business rules.",
                Question.Difficulty.MEDIUM, Question.Source.MANUAL, true);

        Assessment assessment = new Assessment();
        assessment.setTitle("Data Warehousing Fundamentals");
        assessment.setDescription("Mid-semester assessment covering OLAP Operations, ETL Pipelines, and Star Schema concepts.");
        assessment.setSubject(dw);
        assessment.setDurationMinutes(15);
        assessment.setTotalMarks(14);
        assessment.setShuffleQuestions(true);
        assessment.setShuffleOptions(true);
        assessment.setStatus(Assessment.Status.PUBLISHED);
        assessment.setShareToken("8F7K2P");
        assessment.setCreatedBy(faculty);
        assessment.setPublishedAt(LocalDateTime.now().minusDays(1));

        Assessment savedAssessment = assessmentRepository.save(assessment);

        assessmentQuestionRepository.save(new AssessmentQuestion(savedAssessment, q1, 1, 4));
        assessmentQuestionRepository.save(new AssessmentQuestion(savedAssessment, q2, 2, 3));
        assessmentQuestionRepository.save(new AssessmentQuestion(savedAssessment, q3, 3, 3));
        assessmentQuestionRepository.save(new AssessmentQuestion(savedAssessment, q4, 4, 4));
    }

    private void migrateExistingAssessments() {
        List<Assessment> assessments = assessmentRepository.findAll();
        for (Assessment a : assessments) {
            if (a.getStatus() == Assessment.Status.PUBLISHED && (a.getShareToken() == null || a.getShareToken().trim().isEmpty())) {
                a.setShareToken(generateShareToken());
                assessmentRepository.save(a);
            }
        }
    }

    private Question createQuestion(Subject s, Topic t, String text, String a, String b, String c, String d, String ans, String exp, Question.Difficulty diff, Question.Source src, boolean app) {
        Question q = new Question();
        q.setSubject(s);
        q.setTopic(t);
        q.setQuestionText(text);
        q.setOptionA(a);
        q.setOptionB(b);
        q.setOptionC(c);
        q.setOptionD(d);
        q.setCorrectAnswer(ans);
        q.setExplanation(exp);
        q.setDifficulty(diff);
        q.setSource(src);
        q.setApproved(app);
        return questionRepository.save(q);
    }

    private String generateShareToken() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}