import os
from langchain_chroma import Chroma  
from langchain.schema import Document
import uuid 
import logging
import datetime
import pandas as pd
from langchain.schema import Document
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.document_loaders import PyPDFLoader
from langchain.chains.retrieval_qa.base import RetrievalQA
from langchain_ollama import OllamaEmbeddings
from chat import llm

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# Chroma 벡터 DB 설정
CHROMA_DB_DIR = "vectorstore"
PDF_DIR = "pdfs"
embeddings = OllamaEmbeddings(model="llama3.1-instruct-8b:latest")
    

# 벡터 DB 초기화 함수 (한 번만 실행)
def get_vector_db():
    global vector_db
    if vector_db is None:
        vector_db = Chroma(embedding_function=embeddings, persist_directory=CHROMA_DB_DIR)
        logger.info("벡터 DB 연결 완료")
    return vector_db

# 글로벌 변수로 벡터 DB 객체 선언 (싱글턴 패턴으로 관리)
vector_db = None

# 벡터 DB 메시지 저장 함수
def save_to_vector_db(messages, document_type, conversation_id, vector_db):
    try:
        # 벡터 DB 객체 가져오기 (get_vector_db 호출)
        vector_db = get_vector_db()
        
        # 각 메시지별로 벡터화 및 저장
        for message in messages:
            vectors = embeddings.embed_documents([message])

            # 고유 ID 생성 (UUID 사용)
            doc_id = str(uuid.uuid4())  # UUID를 사용하여 고유 ID 생성

            # 메타데이터 추가
            metadata = {
                "id": doc_id,
                "type": document_type,  # 'message', 'pdf', 'web'
                "source": "user_input",
                "conversation_id": conversation_id,  # 대화 ID 추가
                "timestamp": datetime.datetime.now().isoformat()
            }

            # Document 객체 생성
            document = Document(page_content=message, metadata=metadata)

            # 벡터 DB에 문서 추가
            vector_db.add_documents(
                documents=[document],  # Document 객체 전달
                embeddings=vectors,
                ids=[doc_id]  # 고유 ID 전달
            )
            
            logger.info(f"Message saved: {doc_id}, Message: {message}") # 실행된 고유id와 메시지 출력
            
            documents = vector_db.get()
            logger.info(f"전체 조회: {documents}") # 벡터 db에 저장된 전체 정보 출력

        logger.info("벡터 DB에 메시지 저장 성공")
    except Exception as e:
        logger.error(f"벡터 DB 저장 중 오류 발생: {e}")
