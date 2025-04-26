from fastapi import FastAPI, UploadFile, File, Form, Depends, HTTPException
from fastapi.responses import RedirectResponse, JSONResponse
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
from pydantic import BaseModel
from langserve import add_routes
from chat import chain as chat_chain
from dotenv import load_dotenv
from typing import List, Optional
from PIL import Image
import json
import pytesseract
import io
import os
import requests
import uuid
import logging
import datetime
import asyncio
from apscheduler.schedulers.background import BackgroundScheduler
from langchain_chroma import Chroma
from langchain_ollama import OllamaEmbeddings
from langchain.chains.retrieval_qa.base import RetrievalQA
from es_utils import search_documents_es, save_to_elasticsearch
from chat import llm
from data import fetch_data_prec, fetch_data_law, fetch_data_ordin
from rag import save_files_to_vector_db, get_vector_db

# 환경 설정 파일 로딩
load_dotenv()

# 환경 변수 설정
OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL", "http://ollama_container:11434")
ELASTICSEARCH_HOST = os.getenv("ELASTICSEARCH_HOST", "http://es_container:9200")

# FastAPI 애플리케이션 객체 초기화
app = FastAPI()

# 로깅 설정
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")
logger = logging.getLogger(__name__)

logger.info("🚀 서버 시작 중...")

# Elasticsearch 및 Ollama 연결 확인
def check_services():
    """Elasticsearch & Ollama 연결 테스트"""
    try:
        es_response = requests.get(f"{ELASTICSEARCH_HOST}/_cluster/health", timeout=5)
        if es_response.status_code == 200:
            logger.info("✅ Elasticsearch 연결 성공!")
        else:
            logger.warning("⚠️ Elasticsearch 응답 오류: 상태 코드 %d", es_response.status_code)
    except requests.exceptions.RequestException as e:
        logger.error("❌ Elasticsearch 연결 실패: %s", str(e))

    try:
        ollama_response = requests.get(f"{OLLAMA_BASE_URL}/api/tags", timeout=5)
        if ollama_response.status_code == 200:
            logger.info("✅ Ollama 연결 성공!")
        else:
            logger.warning("⚠️ Ollama 응답 오류: 상태 코드 %d", ollama_response.status_code)
    except requests.exceptions.RequestException as e:
        logger.error("❌ Ollama 연결 실패: %s", str(e))


# Tesseract OCR 경로 설정
pytesseract.pytesseract.tesseract_cmd = r"C:\Program Files\Tesseract-OCR\tesseract.exe"

# 벡터 DB 설정
CHROMA_DB_DIR = "vectorstore"
PDF_DIR = "pdfs"
embeddings = OllamaEmbeddings(model="llama3.2")

# 기존 DB 디렉토리 삭제
if os.path.exists(CHROMA_DB_DIR):
   import shutil
   shutil.rmtree(CHROMA_DB_DIR)  # 디렉토리 및 그 안의 내용 모두 삭제
    
os.makedirs(CHROMA_DB_DIR, exist_ok=True)

# 벡터 DB 생성 및 retriever 설정
vector_db = get_vector_db()
retriever = vector_db.as_retriever()

qa_chain = RetrievalQA.from_chain_type(
    llm=llm, chain_type="map_reduce", retriever=retriever, return_source_documents=True
)

# 데이터 수집 스케줄러 설정
scheduler = BackgroundScheduler()


def fetch_all_data():
    """법령 데이터 수집 & 벡터 DB 저장"""
    fetch_data_prec()
    fetch_data_law()
    fetch_data_ordin()
    
     # fetch_all_data가 완료된 후에 save_files_to_vector_db 호출
    save_files_to_vector_db()

# 국가법령정보 엑셀 업데이트
scheduler.add_job(fetch_all_data, trigger='interval', hours=24)


# CORS 미들웨어 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # 배포시 도메인
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
    expose_headers=["*"],
)


# Ollama API 호출 함수
def query_ollama(messages: list[str]):
    """Ollama API에 요청을 보내고 응답을 받는 함수"""
    url = f"{OLLAMA_BASE_URL}/api/generate"
    payload = {
        "model": "llama3.2",
        "prompt": "\n".join(messages),
        "stream": False
    }
    
    try:
        response = requests.post(url, json=payload)
        logger.error(f"response_data값 확인: {response.text}")
        
        response.raise_for_status()
        
        response_data = response.json()
        

        # 응답이 딕셔너리 형식인지 확인 후 처리
        if not isinstance(response_data, dict):
            logger.error(f"❌ Ollama 응답이 예상한 형식이 아닙니다: {response_data}")
            response_data = {"response": str(response_data)}

        chatbot_response = response_data.get("response", "⚠️ Ollama 응답 없음")
        return chatbot_response

    except requests.exceptions.Timeout:
        logger.error("❌ Ollama API 요청 시간 초과!")
        return "⛔ Ollama 응답이 지연되었습니다."

    except requests.exceptions.ConnectionError:
        logger.error("❌ Ollama 서버에 연결할 수 없습니다.")
        return "⛔ Ollama 서버에 연결할 수 없습니다."

    except requests.exceptions.RequestException as e:
        logger.error(f"❌ Ollama API 호출 실패: {e}")
        return f"⛔ Ollama API 오류: {e}"



# 기본 경로("/")
@app.get("/")
async def redirect_root_to_docs():
    return RedirectResponse("/prompt/playground")

@app.get("/prompt/playground")
async def playground():
    return {"message": "Welcome to the Playground!"}

########### 대화형 인터페이스 ###########

# 입력 데이터 모델
class InputChat(BaseModel):
    messages: list[str]

# 대화형 API 엔드포인트
@app.post("/chat")
async def chat(input: str = Form(...), file: Optional[UploadFile] = File(None),  document_type: str = "message", vector_db: Chroma = Depends(get_vector_db)):
    # vector_db는 이제 Chroma 객체로 자동 주입됨
    try:
        input_data = InputChat(**json.loads(input))
        result = {}

        # 대화 ID 생성
        conversation_id = str(uuid.uuid4())

        # OCR 이미지 처리 (이미지 업로드 시 OCR 텍스트 처리)
        if file:
            image = Image.open(io.BytesIO(await file.read()))
            ocr_text = pytesseract.image_to_string(image, lang="kor+eng")
            input_data.messages.append(ocr_text)
            result["ocr_text"] = ocr_text
            
        
        # # 벡터 DB에서 관련 문서 검색
        # query = input_data.messages[-1]  # 최신 메시지 사용
        # docs = retriever.invoke(query)

        # # 검색된 문서를 LLM 입력에 추가
        # context = "\n\n".join([doc.page_content for doc in docs]) if docs else "관련 정보 없음"
        # input_data.messages.append(f"🔍 참고 정보:\n{context}")

        # # 챗봇 응답 생성
        # result["chatbot_response"] = chat_chain.invoke(input_data.messages)    
        
        # 챗봇 응답만 생성 프롬프팅 Test
        # result["chatbot_response"] = chat_chain.invoke(input_data.messages)


        # ES & Chroma 검색 (비동기 처리 개선)
        query = input_data.messages[-1]
        logger.info("🔍 ES & Chroma 검색 시작")

        try:
            # 비동기 작업 순차적으로 처리 (문제 발생 시 각 검색 결과를 하나씩 처리)
            chroma_results = await asyncio.to_thread(retriever.invoke, query)
            logger.info(f"{chroma_results}")
            es_results = await asyncio.to_thread(search_documents_es, query)
            logger.info(f"{es_results}")

            logger.info("✅ ES & Chroma 검색 완료")
        except Exception as e:
            logger.error(f"❌ ES & Chroma 검색 실패: {e}")
            chroma_results, es_results = [], []

        # 검색 결과 합침
        chroma_texts = [doc.page_content for doc in chroma_results]
        es_texts = [doc["text"] for doc in es_results]
        docs = chroma_texts + es_texts
        context = "\n\n".join(docs) if docs else "관련 정보 없음"
        input_data.messages.append(f"🔍 참고 정보:\n{context}")

        # Ollama API 호출
        logger.info("🟢 Ollama API 호출 시작")
        chatbot_response = query_ollama(input_data.messages)
        logger.info(f"🟢 Ollama 응답: {chatbot_response}")

        result["chatbot_response"] = chatbot_response
        
        # 벡터 DB에 메시지 저장
        # save_to_vector_db(input_data.messages, document_type, conversation_id, vector_db)

        return JSONResponse(content={"message": "Chat response", "type": document_type, "result": result}, headers={"Content-Type": "application/json; charset=UTF-8"})
        # return JSONResponse(content={"message": "Chat response", "type": document_type, "result": result, "input_messages": input_data.messages})
    
    except Exception as e:
        logger.error(f"❌ 챗봇 처리 중 오류 발생: {e}")
        return JSONResponse(content={"error": str(e)}, status_code=500)


# 대화형 채팅 엔드포인트 설정
add_routes(
    app,
    chat_chain.with_types(input_type=InputChat),
    path="/chat",
    enable_feedback_endpoint=True,
    enable_public_trace_link_endpoint=True,
    playground_type="chat",
)

# 서버 실행 설정
if __name__ == "__main__":
    scheduler.start()
    
    # 데이터 가져오는 함수 실행
    # fetch_all_data()
    
    check_services()  # Ollama & Elasticsearch 연결 테스트
    uvicorn.run(app, host="0.0.0.0", port=8000)
