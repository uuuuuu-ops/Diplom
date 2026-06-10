FROM python:3.11-slim
WORKDIR /app
RUN pip install --no-cache-dir fastapi uvicorn joblib scikit-learn pandas numpy
COPY . .
RUN python train_model.py
EXPOSE 8000
CMD ["python", "api.py"]
