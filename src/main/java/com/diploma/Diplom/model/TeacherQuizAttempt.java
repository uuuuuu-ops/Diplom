package com.diploma.Diplom.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "teacher_quiz_attempts")
public class TeacherQuizAttempt {

    @Id
    private String id;

    private String userId;
    private String applicationId;
    private String topic;           // специализация учителя

    private List<QuizAnswer> answers;
    private int score;              // 0-100
    private boolean passed;         // score >= 60
    private LocalDateTime takenAt;

    @Data
    public static class QuizAnswer {
        private String questionId;
        private int selectedIndex;
        private boolean correct;
    }
}