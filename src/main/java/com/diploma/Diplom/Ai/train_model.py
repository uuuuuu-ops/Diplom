"""
Запуск:
  pip install pandas scikit-learn joblib
  python train_model.py
"""

import pandas as pd
import joblib
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import classification_report

# ── 1. Загрузка данных ────────────────────────────────────────────────────────
import os
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
df = pd.read_csv(os.path.join(BASE_DIR, "AI_Resume_Screening.csv"))
print("Датасет загружен:", df.shape)
print(df.head(3))

# ── 2. Создаём целевую переменную из AI Score ─────────────────────────────────
def score_to_label(score):
    if score >= 80:
        return "STRONG_FIT"
    elif score >= 60:
        return "GOOD_FIT"
    elif score >= 40:
        return "NEEDS_REVIEW"
    else:
        return "WEAK_FIT"

df["label"] = df["AI Score (0-100)"].apply(score_to_label)
print("\nРаспределение меток:")
print(df["label"].value_counts())


edu_rank = {"B.Sc": 1, "B.Tech": 2, "MBA": 3, "M.Tech": 4, "PhD": 5}
df["edu_rank"] = df["Education"].map(edu_rank).fillna(0)

# Сертификации → есть/нет
df["has_cert"] = (df["Certifications"].str.lower() != "none").astype(int)

# Кол-во навыков
df["skills_count"] = df["Skills"].apply(lambda x: len(str(x).split(",")))

# Наличие ключевых навыков для преподавания
teaching_keywords = ["teaching", "python", "java", "machine learning", "deep learning",
                     "nlp", "sql", "tensorflow", "pytorch", "data"]
def count_teaching_skills(skills_str):
    skills_lower = str(skills_str).lower()
    return sum(1 for kw in teaching_keywords if kw in skills_lower)

df["teaching_skills"] = df["Skills"].apply(count_teaching_skills)

# Recruiter Decision → 0/1
df["recruiter_hire"] = (df["Recruiter Decision"] == "Hire").astype(int)

# ── 4. Признаки для модели ───────────────────────────────────────────────────
features = [
    "Experience (Years)",
    "edu_rank",
    "has_cert",
    "skills_count",
    "teaching_skills",
    "Projects Count",
    "recruiter_hire",
]

X = df[features]
y = df["label"]

print("\nПризнаки:", features)
print("Размер X:", X.shape)

# ── 5. Обучение ───────────────────────────────────────────────────────────────
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42, stratify=y
)

model = RandomForestClassifier(
    n_estimators=200,
    max_depth=10,
    random_state=42,
    class_weight="balanced"
)
model.fit(X_train, y_train)

# ── 6. Оценка ─────────────────────────────────────────────────────────────────
y_pred = model.predict(X_test)
print("\n=== Classification Report ===")
print(classification_report(y_test, y_pred))

accuracy = (y_pred == y_test).mean()
print(f"Accuracy: {accuracy:.2%}")

# Feature importance
print("\nВажность признаков:")
for feat, imp in sorted(zip(features, model.feature_importances_), key=lambda x: -x[1]):
    print(f"  {feat:25s}: {imp:.3f}")

# ── 7. Сохранение модели ──────────────────────────────────────────────────────
joblib.dump(model, "resume_model.pkl")
print("\n✅ Модель сохранена: resume_model.pkl")