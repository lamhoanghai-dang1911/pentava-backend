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
    private static final int TASKS_PER_MOOD = 20;

    private final GroqConfig groqConfig;
    private final OkHttpClient httpClient;
    private final Gson gson;

    public GroqDiagnosticService(GroqConfig groqConfig) {
        this.groqConfig = groqConfig;
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
    }

    public DiagnosticReportDTO analyzeDiagnostic(OnboardingAnswerDTO answers) {
        try {
            String prompt = buildDiagnosticPrompt(answers);
            String groqResponse = callGroqAPI(prompt);
            DiagnosticReportDTO result = parseDiagnosticResponse(groqResponse);
            validateTaskCounts(result);
            return result;
        } catch (IOException e) {
            logger.error("Groq API call failed: ", e);
            throw new RuntimeException("Failed to analyze diagnostic", e);
        }
    }

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
        1. The user's primary goal is "%s". Focus your analysis and 4-week plan EXCLUSIVELY on this specific goal.
        2. Generate EXACTLY 60 tasks total, split into 3 mood-based sets, EXACTLY 20 tasks per set:
           - "badTasks": 20 easy, low-effort, comforting micro-actions suitable for a day the user feels bad/low energy.
           - "neutralTasks": 20 medium-effort actions suitable for an average/neutral day.
           - "goodTasks": 20 more ambitious/challenging actions suitable for a day the user feels good/high energy.
        3. Each set of 20 tasks should span 4 weeks of practice (roughly 5 tasks per week difficulty-appropriate), but do NOT include week numbers or prefixes in the text.
        4. CRITICAL: Each task text must be CLEAN, short, specific, actionable, written in Vietnamese, with NO numbering or prefixes like "Tuần 1:", "Task 1:".
        5. Do NOT generate recommendedGoals (keep the array empty or omit it).
        6. Return ONLY valid JSON (no markdown, no code blocks).

        Structure:
        {
            "overallScore": <1-10 as number>,
            "healthStatus": "<excellent|good|fair|poor|critical>",
            "topIssues": ["issue1", "issue2"],
            "recommendedGoals": [],
            "summary": "<analysis in Vietnamese focused strictly on their primary goal>",
            "moodTaskPlan": {
                "goal": "%s",
                "description": "<why this matters for the primary goal>",
                "badTasks": ["...", "... (exactly 20 items)"],
                "neutralTasks": ["...", "... (exactly 20 items)"],
                "goodTasks": ["...", "... (exactly 20 items)"]
            }
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

    private String callGroqAPI(String userPrompt) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", userPrompt);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", groqConfig.getModel());
        requestBody.add("messages", gson.toJsonTree(new Object[]{message}));
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 4000); // tăng vì output lớn hơn hẳn (60 task)

        Request request = new Request.Builder()
                .url(GROQ_API_URL)
                .addHeader("Authorization", "Bearer " + groqConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.get("application/json")
                ))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                logger.error("Groq API error: {} - {}", response.code(), errorBody);
                throw new IOException("Groq API returned: " + response.code());
            }

            String responseBody = response.body().string();

            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            return jsonResponse.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        }
    }

    private DiagnosticReportDTO parseDiagnosticResponse(String groqResponse) {
        try {
            String cleanedResponse = groqResponse;
            int start = cleanedResponse.indexOf('{');
            int end = cleanedResponse.lastIndexOf('}');
            if (start != -1 && end != -1 && end >= start) {
                cleanedResponse = cleanedResponse.substring(start, end + 1);
            } else {
                cleanedResponse = cleanedResponse.replace("```json", "").replace("```", "").trim();
            }

            JsonObject json = JsonParser.parseString(cleanedResponse).getAsJsonObject();

            DiagnosticReportDTO.MoodTaskPlanDTO moodTaskPlan = null;
            if (json.has("moodTaskPlan") && json.get("moodTaskPlan").isJsonObject()) {
                JsonObject planObj = json.getAsJsonObject("moodTaskPlan");
                moodTaskPlan = DiagnosticReportDTO.MoodTaskPlanDTO.builder()
                        .goal(getAsStringOrNull(planObj, "goal"))
                        .description(getAsStringOrNull(planObj, "description"))
                        .badTasks(extractStringArray(planObj, "badTasks"))
                        .neutralTasks(extractStringArray(planObj, "neutralTasks"))
                        .goodTasks(extractStringArray(planObj, "goodTasks"))
                        .build();
            }

            List<String> topIssues = extractStringArray(json, "topIssues");
            List<String> recommendedGoals = extractStringArray(json, "recommendedGoals");

            return DiagnosticReportDTO.builder()
                    .overallScore(json.get("overallScore").getAsDouble())
                    .healthStatus(json.get("healthStatus").getAsString())
                    .topIssues(topIssues)
                    .recommendedGoals(recommendedGoals)
                    .summary(json.get("summary").getAsString())
                    .moodTaskPlan(moodTaskPlan)
                    .build();

        } catch (Exception e) {
            logger.error("Failed to parse Groq response: {}", groqResponse, e);
            throw new RuntimeException("Failed to parse diagnostic response", e);
        }
    }

    private List<String> extractStringArray(JsonObject parent, String key) {
        List<String> result = new ArrayList<>();
        if (parent.has(key) && parent.get(key).isJsonArray()) {
            JsonArray arr = parent.getAsJsonArray(key);
            for (int i = 0; i < arr.size(); i++) {
                result.add(arr.get(i).getAsString());
            }
        }
        return result;
    }

    private String getAsStringOrNull(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    /**
     * Validate đủ 20 task/mood. Nếu thiếu -> ném lỗi để tầng trên xử lý fallback/retry,
     * tránh lưu DB thiếu task mà không hay biết.
     */
    private void validateTaskCounts(DiagnosticReportDTO result) {
        DiagnosticReportDTO.MoodTaskPlanDTO plan = result.getMoodTaskPlan();
        if (plan == null) {
            throw new RuntimeException("Groq response thiếu moodTaskPlan");
        }
        if (plan.getBadTasks() == null || plan.getBadTasks().size() != TASKS_PER_MOOD
                || plan.getNeutralTasks() == null || plan.getNeutralTasks().size() != TASKS_PER_MOOD
                || plan.getGoodTasks() == null || plan.getGoodTasks().size() != TASKS_PER_MOOD) {
            throw new RuntimeException(String.format(
                    "Groq không trả đủ %d task/mood. bad=%d, neutral=%d, good=%d",
                    TASKS_PER_MOOD,
                    plan.getBadTasks() != null ? plan.getBadTasks().size() : 0,
                    plan.getNeutralTasks() != null ? plan.getNeutralTasks().size() : 0,
                    plan.getGoodTasks() != null ? plan.getGoodTasks().size() : 0));
        }
    }
}