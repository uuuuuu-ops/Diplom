package com.diploma.Diplom.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    private String title;
    private String description;
    private String teacherId;
    private String category;
    private String level;
    private String thumbnail;

    private boolean published;

    private boolean free;
    private BigDecimal price;
    private String currency = "USD";

    private List<String> accessibleBySubscriptions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String thumbnailPublicId;
}