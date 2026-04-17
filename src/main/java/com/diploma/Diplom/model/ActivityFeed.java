package com.diploma.Diplom.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "activity_feed")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityFeed {

    @Id
    private String id;

    private String userId;

    private String type; 

    private String referenceId; 

    private String message;

    private LocalDateTime createdAt;
}
