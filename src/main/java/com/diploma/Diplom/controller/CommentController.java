package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.CommentRequest;
import com.diploma.Diplom.dto.CommentResponse;
import com.diploma.Diplom.model.CommentTargetType;
import com.diploma.Diplom.service.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Tag(name = "Comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{type}/{targetId}")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable CommentTargetType type,
            @PathVariable String targetId,
            @RequestBody CommentRequest request,
            Authentication auth
    ) {
        return ResponseEntity.ok(
                commentService.addComment(auth.getName(), type, targetId, request)
        );
    }

    // GET TREE
    @GetMapping("/{type}/{targetId}")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable CommentTargetType type,
            @PathVariable String targetId,
            Authentication auth
    ) {
        return ResponseEntity.ok(
                commentService.getComments(type, targetId, auth.getName())
        );
    }
}