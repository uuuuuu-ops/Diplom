package com.diploma.Diplom.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.diploma.Diplom.model.ActivityFeed;

public interface ActivityFeedRepository extends MongoRepository<ActivityFeed, String> {

    List<ActivityFeed> findByUserIdOrderByCreatedAtDesc(String userId);

    Page<ActivityFeed> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<ActivityFeed> findByUserId(String userId, Pageable pageable);
}