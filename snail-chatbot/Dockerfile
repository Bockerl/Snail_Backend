# FastAPI 실행 환경
FROM python:3.10

# 작업 디렉토리 설정
WORKDIR /app

# 종속성 복사 및 설치
COPY requirements.txt . 
RUN pip install --no-cache-dir -r requirements.txt

# 애플리케이션 코드 복사
COPY . .

# FastAPI 실행 전에 Ollama API와 모델이 준비될 때까지 대기 (한 줄로 변경)
ENTRYPOINT ["/bin/sh", "-c", "echo 'Checking if Ollama API is available...'; until curl -s http://ollama_container:11434/api/tags | grep -q 'llama3.2'; do echo 'Waiting for Ollama API and Model...'; sleep 2; done; echo 'Ollama Model Found! Starting FastAPI...'; exec uvicorn server:app --host 0.0.0.0 --port 8000 --reload"]
