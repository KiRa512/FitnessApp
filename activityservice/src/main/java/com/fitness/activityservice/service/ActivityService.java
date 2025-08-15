package com.fitness.activityservice.service;

import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.mapper.ActivityMapper;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.repo.ActivityRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepo activityRepo;
    private final ActivityMapper activityMapper;
    private final UserValidationService userValidationService;

    public ActivityResponse trackActivity(ActivityRequest activityRequest) {
        boolean userExists = userValidationService.validateUser(activityRequest.getUserId());
        if (!userExists) {
            throw new RuntimeException("User not found");
        }
        Activity activity = activityMapper.toEntity(activityRequest);
        activity = activityRepo.save(activity);
        return activityMapper.toResponse(activity);

    }


    public List<ActivityResponse> getUserActivities(String userId) {

        List<Activity> activities =  activityRepo.findByUserId(userId);
        return activities.stream()
                .map(activityMapper::toResponse)
                .toList();
    }

    public ActivityResponse getActivity(String activityId) {
        Activity activity = activityRepo.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        return activityMapper.toResponse(activity);
    }
}
