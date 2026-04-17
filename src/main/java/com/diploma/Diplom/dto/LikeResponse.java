package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
@Schema(description = "Likes ")
@Data
@AllArgsConstructor
public class LikeResponse {

    @Schema(description = "liked or not")
    private boolean liked;     
    @Schema(description = "total numbers of likes")  
    private long totalLikes;
}
