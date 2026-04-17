package com.diploma.Diplom.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "A record of a teacher's attempt at the qualification quiz")
@Data
@Document(collection = "teacher_quiz_attempts")
public class TeacherQuizAttempt {

    @Schema(description = "A record of a teacher's attempt at the qualification quiz")
    @Id
    private String id;

    @Schema(description = "The ID of the user who attempted the quiz")
    private String userId;

    @Schema(description = "The ID of the teacher application associated with this quiz attempt")
    private String applicationId;

    @Schema(description = "The topic or specialization related to the quiz attempt, e.g. 'Java', 'Python', 'Data Science'")
    private String topic;           

    @Schema(description = "The list of answers provided by the teacher for each quiz question")
    private List<QuizAnswer> answers;

    @Schema(description = "The score achieved by the teacher in the quiz, ranging from 0 to 100")
    private int score;        

    @Schema(description = "Indicates whether the teacher has passed the quiz based on the score and passing threshold")
    private boolean passed;     

    @Schema(description = "The date and time when the quiz was taken")
    private LocalDateTime takenAt;

    @Schema(description = "The date and time when the quiz attempt record was created")
    @Data
    public static class QuizAnswer {

        private String questionId;

        private Integer selectedIndex;
        
        private boolean correct;
    }
}