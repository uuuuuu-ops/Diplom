package com.diploma.Diplom.service;

import com.diploma.Diplom.model.*;
import com.diploma.Diplom.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherQuizService {

    private final TeacherQuizQuestionRepository questionRepository;
    private final TeacherQuizAttemptRepository attemptRepository;
    private final TeacherApplicationRepository applicationRepository;

    public TeacherQuizService(TeacherQuizQuestionRepository questionRepository,
                              TeacherQuizAttemptRepository attemptRepository,
                              TeacherApplicationRepository applicationRepository) {
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.applicationRepository = applicationRepository;
    }

    public List<TeacherQuizQuestion> getQuestions(String applicationId) {
        TeacherApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        String topic = app.getSpecialization();
        List<TeacherQuizQuestion> questions = questionRepository.findByTopicIgnoreCase(topic);

        // Если нет вопросов по точной теме — берём общие (General)
        if (questions.isEmpty()) {
            questions = questionRepository.findByTopicIgnoreCase("General");
        }

        // Перемешиваем и берём 5
        Collections.shuffle(questions);
        return questions.stream().limit(5).collect(Collectors.toList());
    }

    public TeacherQuizAttempt submitQuiz(String userId,
                                         String applicationId,
                                         Map<String, Integer> answers) {

        TeacherApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!app.getUserId().equals(userId)) {
            throw new RuntimeException("Not your application");
        }

        if (attemptRepository.findByApplicationId(applicationId).isPresent()) {
            throw new RuntimeException("Quiz already submitted");
        }

        List<TeacherQuizAttempt.QuizAnswer> quizAnswers = new ArrayList<>();
        int correct = 0;

        for (Map.Entry<String, Integer> entry : answers.entrySet()) {
            TeacherQuizQuestion question = questionRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Question not found: " + entry.getKey()));

            boolean isCorrect = question.getCorrectIndex() == entry.getValue();
            if (isCorrect) correct++;

            TeacherQuizAttempt.QuizAnswer ans = new TeacherQuizAttempt.QuizAnswer();
            ans.setQuestionId(entry.getKey());
            ans.setSelectedIndex(entry.getValue());
            ans.setCorrect(isCorrect);
            quizAnswers.add(ans);
        }

        int score = (correct * 100) / Math.max(answers.size(), 1);
        boolean passed = score >= 60;

        // Сохраняем попытку
        TeacherQuizAttempt attempt = new TeacherQuizAttempt();
        attempt.setUserId(userId);
        attempt.setApplicationId(applicationId);
        attempt.setTopic(app.getSpecialization());
        attempt.setAnswers(quizAnswers);
        attempt.setScore(score);
        attempt.setPassed(passed);
        attempt.setTakenAt(LocalDateTime.now());
        attemptRepository.save(attempt);

        // Обновляем заявку
        app.setQuizScore(score);
        app.setQuizPassed(passed);
        app.setQuizAttemptId(attempt.getId());
        applicationRepository.save(app);

        return attempt;
    }

    public TeacherQuizAttempt getMyAttempt(String applicationId) {
        return attemptRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new RuntimeException("Quiz not taken yet"));
    }
}