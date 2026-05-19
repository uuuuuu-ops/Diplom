package com.diploma.Diplom.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Comment response")
@Data
public class CommentResponse {
    private String id;
    private String content;

    private String authorId;
    private String authorName;

    private String targetId;
    private String parentId;

    private boolean markedAsAnswer;
    private boolean edited;

    private Instant createdAt;

    private boolean canEdit;

    private List<CommentResponse> replies;
}