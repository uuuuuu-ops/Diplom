package com.diploma.Diplom.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import com.diploma.Diplom.model.CommentTargetType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentNotificationMessage implements Serializable {

    private CommentTargetType targetType;
    private String targetId;

    private String authorId;
    private String authorName;

    private String commentPreview; 
}