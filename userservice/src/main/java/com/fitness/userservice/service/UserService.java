package com.fitness.userservice.service;

import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.mapper.UserMapper;
import com.fitness.userservice.model.User;
import com.fitness.userservice.repository.UserRepo;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {
    private final UserRepo userRepo;
    private final UserMapper userMapper;
    public UserService(UserRepo userRepo , UserMapper userMapper) {
        this.userRepo = userRepo;
        this.userMapper = userMapper;
    }

    public UserResponse register(@Valid RegisterRequest registerRequest) {
        // Check if user already exists
        if (userRepo.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        User user = userMapper.toEntity(registerRequest);
        User savedUser = userRepo.save(user);
        return userMapper.toResponse(savedUser);
    }


    public UserResponse getUserProfile(String userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return  userMapper.toResponse(user);

    }

    public Boolean existByUserId(String userId) {
        return userRepo.existsById(userId);
    }
}
