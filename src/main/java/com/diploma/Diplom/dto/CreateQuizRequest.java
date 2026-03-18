package com.diploma.Diplom.dto;

import com.diploma.Diplom.model.QuizQuestion;
import lombok.Data;

import java.util.List;

@Data
public class CreateQuizRequest {

    private String title;
    private List<QuizQuestion> questions;
}