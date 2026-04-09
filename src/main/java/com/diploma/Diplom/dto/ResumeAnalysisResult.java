package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
@Schema(description = "Resume analysis result")
@Data
public class ResumeAnalysisResult {

    @Schema(description = "The overall score for the resume, typically on a scale from 0 to 100")
    private int score;

    @Schema(description = "A brief summary of the resume analysis   results, highlighting key strengths and weaknesses")
    private String summary;

    @Schema(description = "Specific strengths identified in the resume, such as relevant skills, experience, or education")
    private String strengths;

    @Schema(description = "Specific weaknesses identified in the resume, such as gaps in experience or education")
    private String weaknesses;

    @Schema(description = "Recommendations for improving the resume, such as adding specific skills, gaining more experience, or improving formatting")
    private String recommendation;
}