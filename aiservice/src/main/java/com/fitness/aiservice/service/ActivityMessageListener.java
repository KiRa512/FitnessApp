package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendations;
import com.fitness.aiservice.repo.RecommendationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityMessageListener {
    private final ActivityAiService aiService;
    private final RecommendationRepo recommendationRepo;
    @RabbitListener(queues = "activityQueue")
    public void processActivity(Activity activity){
        log.info("Processing activity: {}", activity.getId());
        log.info("Generating recommendations for activity: {}");

        Recommendations recommendations = aiService.generateRecommendations(activity);
        recommendationRepo.save(recommendations);
    }
}
