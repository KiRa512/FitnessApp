package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAiService {
    private final GeminiService geminiService;

    public Recommendations generateRecommendations(Activity activity){
        log.info("Generating recommendations for activity: {}", activity.getId());
        String prompt = createPromptForActivity(activity);
        String aiRecommendation = geminiService.getAnswer(prompt);

        log.info("Generated AI recommendation: {}", aiRecommendation);
        return processAiResponse(activity , aiRecommendation);
    }

    private Recommendations processAiResponse(Activity activity,String aiRecommendation) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(aiRecommendation);
            JsonNode textNode = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            String jsonContent = textNode.asText()
                    .replaceAll("```json\\n", "")
                    .replaceAll("\\n```", "")
                    .trim();
            log.info("AI response for activity {}: {}", activity.getId(), jsonContent);
            JsonNode analysisJson = mapper.readTree(jsonContent);
            JsonNode analysisNode = analysisJson.path("analysis");
            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysis(fullAnalysis, analysisNode, "overall","Overall:");
            addAnalysis(fullAnalysis, analysisNode, "pace","Pace:");
            addAnalysis(fullAnalysis, analysisNode, "heartRate","Heart Rate:");
            addAnalysis(fullAnalysis, analysisNode, "caloriesBurned","Calories Burned:");
            
            List<String> improvements = extractImprovements(analysisJson.path("improvements"));
            List<String> suggestions = extractSuggestions(analysisJson.path("suggestions"));
            List<String> safety = extractSafety(analysisJson.path("safety"));

            return Recommendations.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .createdAt(LocalDateTime.now())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safety(safety)
                    .build();
        } catch (Exception e) {
            log.error("Error processing AI response for activity {}: {}", activity.getId(), e.getMessage());
            return Recommendations.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .createdAt(LocalDateTime.now())
                    .improvements(Collections.emptyList())
                    .suggestions(Collections.emptyList())
                    .safety(Collections.emptyList())
                    .build();
        }
    }

    private List<String> extractSafety(JsonNode safety) {
        List<String> safetyList = new ArrayList<>();
        if(safety.isArray()){
            for (JsonNode safetyItem : safety) {
                safetyList.add(safetyItem.asText());
            }
        }
        return safetyList.isEmpty() ? Collections.singletonList("No safety guidelines provided") : safetyList;
    }

    private List<String> extractSuggestions(JsonNode suggestions) {
        List<String> suggestionList = new ArrayList<>();
        if(suggestions.isArray()){
            for (JsonNode suggestion : suggestions) {
                String workout = suggestion.path("workout").asText();
                String description = suggestion.path("description").asText();
                suggestionList.add(String.format("%s: %s", workout, description));
            }
        }
        return suggestionList.isEmpty() ? Collections.singletonList("No suggestions available") : suggestionList;
    }

    private List<String> extractImprovements(JsonNode improvementsNode) {
        List<String> improvements = new ArrayList<>();
        if(improvementsNode.isArray()){
            for (JsonNode improvement : improvementsNode) {
                String area = improvement.path("area").asText();
                String recommendation = improvement.path("recommendation").asText();
                improvements.add(String.format("%s: %s", area, recommendation));
            }
        }
        return improvements.isEmpty() ? Collections.singletonList("No improvements suggested") : improvements;

    }

    private void addAnalysis(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if(!analysisNode.path(key).isMissingNode()) {
            fullAnalysis.append(prefix)
                    .append(analysisNode.path(key).asText())
                    .append("\n\n");
        }
    }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
        Analyze this fitness activity and provide detailed recommendations in the following EXACT JSON format:
        {
          "analysis": {
            "overall": "Overall analysis here",
            "pace": "Pace analysis here",
            "heartRate": "Heart rate analysis here",
            "caloriesBurned": "Calories analysis here"
          },
          "improvements": [
            {
              "area": "Area name",
              "recommendation": "Detailed recommendation"
            }
          ],
          "suggestions": [
            {
              "workout": "Workout name",
              "description": "Detailed workout description"
            }
          ],
          "safety": [
            "Safety point 1",
            "Safety point 2"
          ]
        }

        Analyze this activity:
        Activity Type: %s
        Duration: %d minutes
        Calories Burned: %d
        Additional Metrics: %s
        
        Provide detailed analysis focusing on performance, improvements, next workout suggestions, and safety guidelines.
        Ensure the response follows the EXACT JSON format shown above.
        """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );
    }
}
