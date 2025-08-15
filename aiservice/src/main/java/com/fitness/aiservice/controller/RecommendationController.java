package com.fitness.aiservice.controller;

import com.fitness.aiservice.model.Recommendations;
import com.fitness.aiservice.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<Recommendations>> getUserRecommendations(@PathVariable String userId) {
        List<Recommendations> userRecommendations = recommendationService.getUserRecommendations(userId);
        return ResponseEntity.ok(userRecommendations);
    }

    @GetMapping("/activity/{activityId}")
    public ResponseEntity<Recommendations> getActivityRecommendations(@PathVariable String activityId) {
        Recommendations activityRecommendations = recommendationService.getActivityRecommendations(activityId);
        return ResponseEntity.ok(activityRecommendations);
    }
}
