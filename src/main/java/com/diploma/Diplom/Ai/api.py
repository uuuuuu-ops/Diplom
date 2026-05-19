"""
Запуск:
  pip install fastapi uvicorn joblib scikit-learn pandas
  python api.py
"""

from fastapi import FastAPI
from pydantic import BaseModel
import joblib
import numpy as np

app = FastAPI(title="Resume AI Screening API")

# Загружаем модель при старте
import os
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
model = joblib.load(os.path.join(BASE_DIR, "resume_model.pkl"))

# Маппинг образования
EDU_RANK = {"B.Sc": 1, "B.Tech": 2, "MBA": 3, "M.Tech": 4, "PhD": 5}

TEACHING_KEYWORDS = [
    "teaching", "python", "java", "machine learning", "deep learning",
    "nlp", "sql", "tensorflow", "pytorch", "data"
]

# ── Схема запроса ─────────────────────────────────────────────────────────────
class ResumeRequest(BaseModel):
    resumeText: str          # текст резюме (из PDF)
    specialization: str      # специализация учителя
    yearsOfExperience: int   # годы опыта
    education: str = "B.Sc" # уровень образования
    certifications: str = "" # сертификации
    projectsCount: int = 0   # кол-во проектов

# ── Схема ответа ──────────────────────────────────────────────────────────────
class ResumeResponse(BaseModel):
    score: int
    recommendation: str
    summary: str
    strengths: str
    weaknesses: str

# ── Логика предсказания ───────────────────────────────────────────────────────
def extract_features(req: ResumeRequest):
    text_lower = req.resumeText.lower()

    edu_rank = EDU_RANK.get(req.education, 1)
    has_cert = 1 if req.certifications and req.certifications.lower() not in ["", "none"] else 0

    # Считаем навыки из текста резюме
    skills_count = len([w for w in text_lower.split(",") if len(w.strip()) > 2])
    skills_count = min(skills_count, 20)  # cap

    teaching_skills = sum(1 for kw in TEACHING_KEYWORDS if kw in text_lower)
    recruiter_hire = 1 if req.yearsOfExperience >= 2 else 0

    return [
        req.yearsOfExperience,
        edu_rank,
        has_cert,
        skills_count,
        teaching_skills,
        req.projectsCount,
        recruiter_hire,
    ]

def build_score(recommendation: str, req: ResumeRequest) -> int:
    base = {"STRONG_FIT": 85, "GOOD_FIT": 70, "NEEDS_REVIEW": 50, "WEAK_FIT": 25}
    score = base.get(recommendation, 50)
    # небольшие корректировки
    if req.yearsOfExperience >= 5:
        score = min(score + 5, 100)
    if req.certifications and req.certifications.lower() not in ["", "none"]:
        score = min(score + 3, 100)
    return score

def build_summary(rec: str, req: ResumeRequest) -> str:
    labels = {
        "STRONG_FIT": "Кандидат отлично подходит для позиции преподавателя.",
        "GOOD_FIT": "Кандидат хорошо подходит, есть небольшие зоны роста.",
        "NEEDS_REVIEW": "Кандидат требует дополнительной проверки.",
        "WEAK_FIT": "Кандидат слабо соответствует требованиям.",
    }
    base = labels.get(rec, "")
    return (
        f"{base} Специализация: {req.specialization}. "
        f"Опыт: {req.yearsOfExperience} лет. "
        f"Образование: {req.education}."
    )

def build_strengths(req: ResumeRequest) -> str:
    text_lower = req.resumeText.lower()
    found = []
    if req.yearsOfExperience >= 3:
        found.append(f"Опыт {req.yearsOfExperience} лет")
    if req.certifications and req.certifications.lower() not in ["", "none"]:
        found.append(f"Сертификация: {req.certifications}")
    if req.projectsCount > 0:
        found.append(f"Проекты: {req.projectsCount}")
    for kw in TEACHING_KEYWORDS:
        if kw in text_lower:
            found.append(f"Навык: {kw}")
    if req.education in ["PhD", "M.Tech"]:
        found.append(f"Высокий уровень образования: {req.education}")
    return ". ".join(found) if found else "Базовые данные присутствуют."

def build_weaknesses(req: ResumeRequest) -> str:
    text_lower = req.resumeText.lower()
    issues = []
    if req.yearsOfExperience < 1:
        issues.append("Нет опыта работы")
    if not req.certifications or req.certifications.lower() in ["", "none"]:
        issues.append("Нет сертификаций")
    if req.projectsCount == 0:
        issues.append("Нет проектов в портфолио")
    if len(req.resumeText) < 200:
        issues.append("Резюме слишком короткое")
    if not any(kw in text_lower for kw in TEACHING_KEYWORDS):
        issues.append("Не найдено ключевых технических навыков")
    return ". ".join(issues) if issues else "Явных слабостей не обнаружено."

# ── Эндпоинты ─────────────────────────────────────────────────────────────────
@app.get("/health")
def health():
    return {"status": "ok", "model": "RandomForest Resume Screener"}

@app.post("/analyze", response_model=ResumeResponse)
def analyze_resume(req: ResumeRequest):
    features = extract_features(req)
    prediction = model.predict([features])[0]
    score = build_score(prediction, req)

    return ResumeResponse(
        score=score,
        recommendation=prediction,
        summary=build_summary(prediction, req),
        strengths=build_strengths(req),
        weaknesses=build_weaknesses(req),
    )

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
