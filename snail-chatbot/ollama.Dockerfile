# Ollama의 공식 이미지 사용
FROM ollama/ollama:latest

# 필요한 패키지 설치
RUN apt-get update && apt-get install -y curl

# Ollama 서버 실행
CMD ["sh", "-c", "ollama serve"]
