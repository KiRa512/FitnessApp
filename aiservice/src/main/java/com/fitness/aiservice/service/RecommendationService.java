package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Recommendations;
import com.fitness.aiservice.repo.RecommendationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final RecommendationRepo recommendationRepo;

    public List<Recommendations> getUserRecommendations(String userId) {
        return recommendationRepo.findByUserId(userId);
    }

    public Recommendations getActivityRecommendations(String activityId) {
        return recommendationRepo.findByActivityId(activityId)
                .orElseThrow(() -> new RuntimeException("Recommendations not found for activity: " + activityId));
    }
}
