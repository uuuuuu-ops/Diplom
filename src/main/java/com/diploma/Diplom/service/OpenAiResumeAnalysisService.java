package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.ResumeAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.time.Duration;

@Slf4j
@Service
public class OpenAiResumeAnalysisService {

    @Value("${ai.resume.api.url}")
    private String aiApiUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResumeAnalysisResult analyzeResume(
            String resumeText,
            String specialization,
            int yearsOfExperience) {

        try {
            Map<String, Object> requestBody = Map.of(
                    "resumeText", resumeText != null ? resumeText : "",
                    "specialization", specialization != null ? specialization : "",
                    "yearsOfExperience", yearsOfExperience,
                    "education", extractEducation(resumeText),
                    "certifications", extractCertifications(resumeText),
                    "projectsCount", extractProjectsCount(resumeText)
            );

            String json = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiApiUrl + "/analyze"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                return parseResponse(response.body());
            } else {
                log.error("AI API error: status={}, body={}", response.statusCode(), response.body());
                return fallbackResult(resumeText, specialization, yearsOfExperience);
            }

        } catch (Exception e) {
            log.warn("AI API unavailable: {}", e.getMessage());
            return fallbackResult(resumeText, specialization, yearsOfExperience);
        }
    }

    private ResumeAnalysisResult parseResponse(String json) throws Exception {
        var node = objectMapper.readTree(json);
        ResumeAnalysisResult result = new ResumeAnalysisResult();
        result.setScore(node.get("score").asInt());
        result.setRecommendation(node.get("recommendation").asText());
        result.setSummary(node.get("summary").asText());
        result.setStrengths(node.get("strengths").asText());
        result.setWeaknesses(node.get("weaknesses").asText());
        return result;
    }

    private ResumeAnalysisResult fallbackResult(
            String resumeText,
            String specialization,
            int yearsOfExperience) {

        ResumeAnalysisResult result = new ResumeAnalysisResult();
        int score = 30;
        if (yearsOfExperience >= 1) score += 15;
        if (yearsOfExperience >= 3) score += 15;
        if (resumeText != null && resumeText.length() > 300) score += 20;
        if (specialization != null && !specialization.isBlank()) score += 10;
        score = Math.min(score, 100);

        result.setScore(score);
        result.setSummary("Базовый анализ (AI сервис недоступен). Опыт: " + yearsOfExperience + " лет.");
        result.setStrengths(yearsOfExperience >= 2 ? "Есть опыт работы." : "Начинающий кандидат.");
        result.setWeaknesses(yearsOfExperience < 1 ? "Нет опыта работы." : "");
        result.setRecommendation(score >= 70 ? "GOOD_FIT" : score >= 50 ? "NEEDS_REVIEW" : "WEAK_FIT");
        return result;
    }

    private String extractEducation(String text) {
        if (text == null) return "B.Sc";
        String lower = text.toLowerCase();
        if (lower.contains("phd") || lower.contains("doctorate")) return "PhD";
        if (lower.contains("m.tech") || lower.contains("master")) return "M.Tech";
        if (lower.contains("mba")) return "MBA";
        if (lower.contains("b.tech")) return "B.Tech";
        return "B.Sc";
    }

    private String extractCertifications(String text) {
        if (text == null) return "None";
        String lower = text.toLowerCase();
        if (lower.contains("certified") || lower.contains("certification") || lower.contains("certificate")) {
            return "Yes";
        }
        return "None";
    }

    private int extractProjectsCount(String text) {
        if (text == null) return 0;
        String lower = text.toLowerCase();
        if (lower.contains("project")) {
            int count = 0;
            int idx = 0;
            while ((idx = lower.indexOf("project", idx)) != -1) {
                count++;
                idx += 7;
            }
            return Math.min(count, 10);
        }
        return 0;
    }
}