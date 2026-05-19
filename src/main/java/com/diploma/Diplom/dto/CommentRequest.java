package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "Comment creation or edit request")
@Data
public class CommentRequest {

    @Schema(description = "The text content of the comment or reply")
    @NotBlank(message = "Comment content is required")
    @Size(max = 1000, message = "Comment too long")
    private String content;

    @Schema(description = "The ID of the parent comment (null for top-level comments)")
    private String parentId;
}