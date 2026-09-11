package com.nqt.onboarding.infrastructure.groq;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nqt.onboarding.infrastructure.configuration.GroqConfig;
import com.nqt.onboarding.usecase.dto.DiagnosticReportDTO;
import com.nqt.onboarding.usecase.dto.OnboardingAnswerDTO;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GroqDiagnosticService {

    private static final Logger logger = LoggerFactory.getLogger(GroqDiagnosticService.class);
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    private final GroqConfig groqConfig;
    private final OkHttpClient httpClient;
    private final Gson gson;

    public GroqDiagnosticService(GroqConfig groqConfig) {
        this.groqConfig = groqConfig;
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
    }

    /**
     * Phân tích onboarding answers và trả về diagnostic report
     */
    public DiagnosticReportDTO analyzeDiagnostic(OnboardingAnswerDTO answers) {
        try {
            String prompt = buildDiagnosticPrompt(answers);
            String groqResponse = callGroqAPI(prompt);
            return parseDiagnosticResponse(groqResponse);
        } catch (IOException e) {
            logger.error("Groq API call failed: ", e);
            throw new RuntimeException("Failed to analyze diagnostic", e);
        }
    }

    /**
     * Build prompt cho Groq LLM
     */
    private String buildDiagnosticPrompt(OnboardingAnswerDTO answers) {
        return String.format("""
        You are a health and wellness diagnostic AI. Analyze the following user health profile and provide a diagnostic report in JSON format.

        User Profile:
        - Name: %s
        - Age: %s
        - Gender: %s
        - Primary Goal: %s
        - Preferred Routine Type: %s
        - Sleep Hours: %s
        - Exercise Frequency: %s
        - Stress Frequency: %s
        - Screen Time: %s
        - Habit Retention: %s
        - Free Time Slots: %s

        IMPORTANT RULES:
        1. The user's primary goal is "%s". Focus your analysis and 4-week action plan EXCLUSIVELY on this specific goal.
        2. Provide ONLY ONE "actionPlans" object, but inside its "actions" array, you MUST generate EXACTLY 8 tasks in total to cover the 4 weeks (meaning 2 tasks per week: 2 tasks for Week 1, 2 tasks for Week 2, 2 tasks for Week 3, and 2 tasks for Week 4).
        3. CRITICAL: The "content" field for each action must be CLEAN, short, and SPECIFIC. DO NOT include prefixes like "Week 1:", "Tuần 1:", or numbering inside the content text.
        4. Do NOT generate recommendedGoals (keep the array empty or omit it).
        5. Return ONLY valid JSON (no markdown, no code blocks).

        Structure:
        {
            "overallScore": <1-10 as number>,
            "healthStatus": "<excellent|good|fair|poor|critical>",
            "topIssues": ["issue1", "issue2"],
            "recommendedGoals": [],
            "summary": "<analysis in Vietnamese focused strictly on their primary goal>",
            "actionPlans": [
                {
                    "goal": "%s",
                    "description": "<why this matters for the primary goal>",
                    "actions": [
                        "Task 1 for week 1 (pure text)",
                        "Task 2 for week 1 (pure text)",
                        "Task 1 for week 2 (pure text)",
                        "Task 2 for week 2 (pure text)",
                        "Task 1 for week 3 (pure text)",
                        "Task 2 for week 3 (pure text)",
                        "Task 1 for week 4 (pure text)",
                        "Task 2 for week 4 (pure text)"
                    ]
                }
            ]
        }

        Return ONLY JSON, nothing else.
        """,
                answers.getName(),
                answers.getAge(),
                answers.getGender(),
                answers.getPrimaryGoal(),
                answers.getRoutineType(),
                answers.getSleepHours(),
                answers.getExerciseFrequency(),
                answers.getStressFrequency(),
                answers.getScreenTime(),
                answers.getHabitDuration(),
                answers.getFreeTimes() != null ? String.join(", ", answers.getFreeTimes()) : "",
                answers.getPrimaryGoal(),
                answers.getPrimaryGoal()
        );
    }

    /**
     * Call Groq API
     */
    private String callGroqAPI(String userPrompt) throws IOException {
        // Build message
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", userPrompt);

        // Build request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", groqConfig.getModel());
        requestBody.add("messages", gson.toJsonTree(new Object[]{message}));
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 2000);

        // Create HTTP request
        Request request = new Request.Builder()
                .url(GROQ_API_URL)
                .addHeader("Authorization", "Bearer " + groqConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.get("application/json")
                ))
                .build();

        // Execute request
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                logger.error("Groq API error: {} - {}", response.code(), errorBody);
                throw new IOException("Groq API returned: " + response.code());
            }

            String responseBody = response.body().string();
            logger.debug("Groq API response: {}", responseBody);

            // Parse response
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            return jsonResponse.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        }
    }

    /**
     * Parse Groq response thành DiagnosticReportDTO
     */
    private DiagnosticReportDTO parseDiagnosticResponse(String groqResponse) {
        try {
            // Clean response (remove markdown if exists)
            String cleanedResponse = groqResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            JsonObject json = JsonParser.parseString(cleanedResponse).getAsJsonObject();

            // Parse action plans
            List<DiagnosticReportDTO.ActionPlanDTO> actionPlans = new ArrayList<>();
            if (json.has("actionPlans") && json.get("actionPlans").isJsonArray()) {
                JsonArray plansArray = json.getAsJsonArray("actionPlans");
                for (int i = 0; i < plansArray.size(); i++) {
                    JsonObject planObj = plansArray.get(i).getAsJsonObject();
                    List<String> actions = new ArrayList<>();

                    if (planObj.has("actions") && planObj.get("actions").isJsonArray()) {
                        JsonArray actionsArray = planObj.getAsJsonArray("actions");
                        for (int j = 0; j < actionsArray.size(); j++) {
                            actions.add(actionsArray.get(j).getAsString());
                        }
                    }

                    actionPlans.add(DiagnosticReportDTO.ActionPlanDTO.builder()
                            .goal(planObj.get("goal").getAsString())
                            .description(planObj.get("description").getAsString())
                            .actions(actions)
                            .build());
                }
            }

            // Parse top issues
            List<String> topIssues = new ArrayList<>();
            if (json.has("topIssues") && json.get("topIssues").isJsonArray()) {
                JsonArray issuesArray = json.getAsJsonArray("topIssues");
                for (int i = 0; i < issuesArray.size(); i++) {
                    topIssues.add(issuesArray.get(i).getAsString());
                }
            }

            // Parse recommended goals
            List<String> recommendedGoals = new ArrayList<>();
            if (json.has("recommendedGoals") && json.get("recommendedGoals").isJsonArray()) {
                JsonArray goalsArray = json.getAsJsonArray("recommendedGoals");
                for (int i = 0; i < goalsArray.size(); i++) {
                    recommendedGoals.add(goalsArray.get(i).getAsString());
                }
            }

            return DiagnosticReportDTO.builder()
                    .overallScore(json.get("overallScore").getAsDouble())
                    .healthStatus(json.get("healthStatus").getAsString())
                    .topIssues(topIssues)
                    .recommendedGoals(recommendedGoals)
                    .summary(json.get("summary").getAsString())
                    .actionPlans(actionPlans)
                    .build();

        } catch (Exception e) {
            logger.error("Failed to parse Groq response: {}", groqResponse, e);
            throw new RuntimeException("Failed to parse diagnostic response", e);
        }
    }
}