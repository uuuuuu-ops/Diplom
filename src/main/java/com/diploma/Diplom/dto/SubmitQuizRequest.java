package com.diploma.Diplom.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubmitQuizRequest {

    private String quizId;
    private List<Integer> answers;
}