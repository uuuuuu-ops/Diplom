package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.ResumeAnalysisResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenAiResumeAnalysisService {

    private static final List<String> EDUCATION_KEYWORDS = List.of(
            "education", "academic", "university", "college", "degree", "bachelor", "master", "phd",
            "образование", "университет", "колледж", "бакалавр", "магистр", "докторант", "диплом"
    );

    private static final List<String> TEACHING_KEYWORDS = List.of(
            "teaching", "teacher", "tutor", "lecturer", "instructor", "mentor", "training", "pedagogy",
            "преподав", "учител", "обуч", "лектор", "наставник", "ментор", "педагог"
    );

    private static final List<String> EXPERIENCE_KEYWORDS = List.of(
            "experience", "work experience", "employment", "career", "worked", "internship",
            "опыт", "опыт работы", "стаж", "карьера", "работал", "работала", "стажировка"
    );

    private static final List<String> SKILLS_KEYWORDS = List.of(
            "skills", "technical skills", "competencies", "stack", "technologies",
            "навыки", "компетенции", "технологии", "стек"
    );

    private static final List<String> PROJECTS_KEYWORDS = List.of(
            "projects", "project", "portfolio",
            "проекты", "проект", "портфолио"
    );

    private static final List<String> CONTACT_KEYWORDS = List.of(
            "email", "phone", "telegram", "linkedin", "github", "contact",
            "почта", "телефон", "контакт", "связь"
    );

    private static final List<String> JAVA_KEYWORDS = List.of(
            "java", "core java", "java se", "java ee"
    );

    private static final List<String> SPRING_KEYWORDS = List.of(
            "spring", "spring boot", "spring mvc", "spring data", "spring security"
    );

    private static final List<String> DATABASE_KEYWORDS = List.of(
            "sql", "mysql", "postgresql", "mongodb", "database", "oracle", "sqlite",
            "база данных", "бд"
    );

    private static final List<String> BACKEND_KEYWORDS = List.of(
            "backend", "rest api", "api", "microservices", "server-side",
            "бэкенд", "сервер", "микросервис"
    );

    private static final List<String> CERTIFICATION_KEYWORDS = List.of(
            "certificate", "certification", "certified",
            "сертификат", "сертификация"
    );

    public ResumeAnalysisResult analyzeResume(String resumeText, String specialization, int yearsOfExperience) {
        ResumeAnalysisResult result = new ResumeAnalysisResult();

        String text = normalize(resumeText);

        boolean hasEducation = containsAny(text, EDUCATION_KEYWORDS);
        boolean hasTeaching = containsAny(text, TEACHING_KEYWORDS);
        boolean hasExperience = containsAny(text, EXPERIENCE_KEYWORDS);
        boolean hasSkills = containsAny(text, SKILLS_KEYWORDS);
        boolean hasProjects = containsAny(text, PROJECTS_KEYWORDS);
        boolean hasContacts = containsAny(text, CONTACT_KEYWORDS);

        boolean hasJava = containsAny(text, JAVA_KEYWORDS);
        boolean hasSpring = containsAny(text, SPRING_KEYWORDS);
        boolean hasDatabase = containsAny(text, DATABASE_KEYWORDS);
        boolean hasBackend = containsAny(text, BACKEND_KEYWORDS);
        boolean hasCertification = containsAny(text, CERTIFICATION_KEYWORDS);

        int score = 0;

        // Base completeness
        if (!text.isBlank()) score += 10;
        if (text.length() >= 200) score += 10;
        if (text.length() >= 500) score += 10;
        if (text.length() >= 900) score += 5;

        // Structure
        if (hasContacts) score += 5;
        if (hasEducation) score += 10;
        if (hasExperience) score += 10;
        if (hasSkills) score += 10;
        if (hasProjects) score += 5;

        // Teaching profile
        if (hasTeaching) score += 15;

        // Technical profile
        if (hasJava) score += 5;
        if (hasSpring) score += 5;
        if (hasDatabase) score += 5;
        if (hasBackend) score += 5;

        // Certifications
        if (hasCertification) score += 5;

        // Experience years
        if (yearsOfExperience >= 1) score += 5;
        if (yearsOfExperience >= 3) score += 5;
        if (yearsOfExperience >= 5) score += 5;

        // Specialization filled
        if (specialization != null && !specialization.isBlank()) {
            score += 5;
        }

        if (score > 100) {
            score = 100;
        }

        result.setScore(score);
        result.setSummary(buildSummary(score, hasTeaching, hasEducation, hasExperience, hasJava, hasSpring, yearsOfExperience, specialization));
        result.setStrengths(buildStrengths(hasContacts, hasEducation, hasExperience, hasSkills, hasProjects,
                hasTeaching, hasJava, hasSpring, hasDatabase, hasBackend, hasCertification, yearsOfExperience));
        result.setWeaknesses(buildWeaknesses(text, hasContacts, hasEducation, hasExperience, hasSkills, hasProjects,
                hasTeaching, hasCertification, yearsOfExperience));
        result.setRecommendation(buildRecommendation(score, hasTeaching, hasEducation, hasExperience));

        return result;
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }

        return text.toLowerCase()
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean containsAny(String text, List<String> keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String buildSummary(int score,
                                boolean hasTeaching,
                                boolean hasEducation,
                                boolean hasExperience,
                                boolean hasJava,
                                boolean hasSpring,
                                int yearsOfExperience,
                                String specialization) {
        StringBuilder sb = new StringBuilder();

        sb.append("Automatic bilingual resume analysis completed. ");
        sb.append("Candidate score: ").append(score).append("/100. ");

        if (specialization != null && !specialization.isBlank()) {
            sb.append("Specialization: ").append(specialization).append(". ");
        }

        if (yearsOfExperience > 0) {
            sb.append("Declared experience: ").append(yearsOfExperience).append(" years. ");
        }

        if (hasTeaching) {
            sb.append("Teaching-related background detected. ");
        } else {
            sb.append("Teaching background is not clearly expressed. ");
        }

        if (hasEducation) {
            sb.append("Education information is present. ");
        }

        if (hasExperience) {
            sb.append("Work experience section or signals detected. ");
        }

        if (hasJava) {
            sb.append("Java skill detected. ");
        }

        if (hasSpring) {
            sb.append("Spring ecosystem mentioned. ");
        }

        return sb.toString().trim();
    }

    private String buildStrengths(boolean hasContacts,
                                  boolean hasEducation,
                                  boolean hasExperience,
                                  boolean hasSkills,
                                  boolean hasProjects,
                                  boolean hasTeaching,
                                  boolean hasJava,
                                  boolean hasSpring,
                                  boolean hasDatabase,
                                  boolean hasBackend,
                                  boolean hasCertification,
                                  int yearsOfExperience) {
        StringBuilder sb = new StringBuilder();

        if (hasContacts) sb.append("Contact information is present. ");
        if (hasEducation) sb.append("Education section detected. ");
        if (hasExperience) sb.append("Experience information detected. ");
        if (hasSkills) sb.append("Skills section detected. ");
        if (hasProjects) sb.append("Projects or portfolio mentioned. ");
        if (hasTeaching) sb.append("Teaching or mentoring background found. ");
        if (hasJava) sb.append("Java knowledge detected. ");
        if (hasSpring) sb.append("Spring framework knowledge detected. ");
        if (hasDatabase) sb.append("Database knowledge detected. ");
        if (hasBackend) sb.append("Backend development skills detected. ");
        if (hasCertification) sb.append("Certificates or certifications mentioned. ");
        if (yearsOfExperience >= 1) sb.append("Practical experience provided. ");

        if (sb.length() == 0) {
            sb.append("Basic candidate data is present, but strong highlights were not clearly detected.");
        }

        return sb.toString().trim();
    }

    private String buildWeaknesses(String text,
                                   boolean hasContacts,
                                   boolean hasEducation,
                                   boolean hasExperience,
                                   boolean hasSkills,
                                   boolean hasProjects,
                                   boolean hasTeaching,
                                   boolean hasCertification,
                                   int yearsOfExperience) {
        StringBuilder sb = new StringBuilder();

        if (text.length() < 200) sb.append("Resume text is too short. ");
        if (!hasContacts) sb.append("Contact information is missing or unclear. ");
        if (!hasEducation) sb.append("Education section is missing or unclear. ");
        if (!hasExperience) sb.append("Experience section is missing or unclear. ");
        if (!hasSkills) sb.append("Skills section is missing or unclear. ");
        if (!hasProjects) sb.append("Projects or portfolio are not clearly described. ");
        if (!hasTeaching) sb.append("Teaching or mentoring background is not clearly described. ");
        if (!hasCertification) sb.append("No certifications were detected. ");
        if (yearsOfExperience == 0) sb.append("No years of experience were specified. ");

        if (sb.length() == 0) {
            sb.append("No major weaknesses detected during automatic screening.");
        }

        return sb.toString().trim();
    }

    private String buildRecommendation(int score,
                                       boolean hasTeaching,
                                       boolean hasEducation,
                                       boolean hasExperience) {
        if (score >= 80 && hasTeaching && hasEducation && hasExperience) {
            return "STRONG_FIT";
        }
        if (score >= 60) {
            return "GOOD_FIT";
        }
        if (score >= 40) {
            return "NEEDS_REVIEW";
        }
        return "WEAK_FIT";
    }
}