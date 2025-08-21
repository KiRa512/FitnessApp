package com.fitness.gateway.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {
    private final WebClient userServiceWebClient;

    public Mono<Boolean> validateUser(String userId){
        return userServiceWebClient.get().uri("/api/users/{userId}/validate", userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(WebClientResponseException.class , e->{
                    if(e.getStatusCode() == HttpStatus.NOT_FOUND)
                        return Mono.error(new RuntimeException("User not found: " + userId));
                    else if(e.getStatusCode() == HttpStatus.BAD_REQUEST)
                        return Mono.error(new RuntimeException("invalid request for user: " + userId));
                    else
                        return Mono.error(new RuntimeException("Error validating user: " + e.getMessage()));
                });
    }

    public Mono<UserResponse> registerUser(RegisterRequest request) {
        return userServiceWebClient.post().uri("/api/users/{userId}/validate", request.getEmail())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(WebClientResponseException.class , e->{
                    if(e.getStatusCode() == HttpStatus.BAD_REQUEST)
                        return Mono.error(new RuntimeException("Bad Request: " + e.getMessage()));
                    else if(e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
                        return Mono.error(new RuntimeException("internal Server Error: " + e.getMessage()));
                    else
                        return Mono.error(new RuntimeException("Error validating user: " + e.getMessage()));
                });
    }
}
