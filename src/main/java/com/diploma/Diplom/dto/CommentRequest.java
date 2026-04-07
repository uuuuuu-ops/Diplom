package com.diploma.Diplom.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Comment creation request")
@Data
public class CommentRequest {
    @Schema(description = "The content of the comment or reply")
    private String content;
    @Schema(description = "The ID of the parent comment (null for top-level comments)")
    private String parentId;
}