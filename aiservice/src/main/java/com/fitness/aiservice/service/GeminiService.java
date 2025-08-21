package com.fitness.aiservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.ObjectError;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class GeminiService {
    private final WebClient webClient;
    @Value("${GEMINI_API_URL}")
    private String geminiApiUrl;
    @Value("${GEMINI_API_KEY}")
    private String geminiApiKey;

    public GeminiService(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.build();
    }

    public String getAnswer(String qestion){
        Map<String , Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts",new Object[]{
                                Map.of("text", qestion)
                        })
            }
        );
        String response = webClient.post()
                .uri(geminiApiUrl + "?key=" + geminiApiKey) // full URL + query param
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return response;
    }


}
