package com.diploma.Diplom.service;

import com.diploma.Diplom.model.Quiz;
import com.diploma.Diplom.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizService {

    private final QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    public Quiz createQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    public List<Quiz> getQuizzesByLesson(String lessonId) {
        return quizRepository.findByLessonId(lessonId);
    }
}