import os
import requests
from langchain_elasticsearch import ElasticsearchStore
from langchain_ollama import OllamaEmbeddings

# 환경 변수에서 Elasticsearch 주소 가져오기
es_url = os.getenv("ELASTICSEARCH_HOST", "http://es_container:9200")
index_name = "chatbot_index"
embedding_function = OllamaEmbeddings(model="llama3.2")  # 모델 로딩

# Elasticsearch 인덱스 생성 함수
def create_es_index():
    """Elasticsearch 인덱스를 생성 (존재 여부 확인 후 생성)"""
    print("🔍 인덱스 생성 시작...")

    # 인덱스 존재 여부 확인
    response = requests.head(f"{es_url}/{index_name}")
    if response.status_code == 200:
        print(f"✅ Index '{index_name}' already exists.")
        return

    index_mapping = {
        "mappings": {
            "properties": {
                "conversation_id": {"type": "keyword"},
                "text": {"type": "text"},
                "embedding": {
                    "type": "dense_vector",
                    "dims": 768,  # 벡터 차원
                    "index": True,
                    "similarity": "cosine"
                }
            }
        }
    }

    print("🚀 인덱스 생성 요청 중...")
    response = requests.put(f"{es_url}/{index_name}", json=index_mapping)

    if response.status_code not in [200, 201]:
        print("❌ 인덱스 생성 실패! 오류 메시지:", response.json())
    else:
        print("✅ 인덱스 생성 성공:", response.json())

# ElasticsearchStore 설정
es_store = ElasticsearchStore(
    index_name=index_name,
    es_url=es_url
)

# ES 검색 함수 (Chroma와 함께 병행 검색할 때 사용)
def search_documents_es(query):
    """Elasticsearch 벡터 검색 실행 (knn 방식)"""
    try:
        query_vector = embedding_function.embed_query(query)  # 쿼리 벡터화
    except Exception as e:
        print(f"❌ 벡터화 오류: {e}")
        return []

    es_query = {
        "size": 5,
        "query": {
            "knn": {
                "embedding": {
                    "vector": query_vector,
                    "k": 5,
                    "num_candidates": 50  # 더 높은 값으로 설정하면 더 정확한 검색 가능
                }
            }
        }
    }

    response = requests.post(f"{es_url}/{index_name}/_search", json=es_query)
    results = response.json().get("hits", {}).get("hits", [])

    return [hit["_source"] for hit in results]  # 결과 반환

# Elasticsearch에 대화 내용을 저장
def save_to_elasticsearch(messages, document_type, conversation_id):
    """Elasticsearch에 대화 내용을 벡터와 함께 저장"""
    try:
        text_content = "\n".join(messages)
        embedding_vector = embedding_function.embed_query(text_content)
    except Exception as e:
        print(f"❌ 임베딩 오류: {e}")
        return {}

    doc = {
        "conversation_id": conversation_id,
        "text": text_content,
        "embedding": embedding_vector,  
        "document_type": document_type
    }

    response = requests.post(f"{es_url}/{index_name}/_doc", json=doc)
    return response.json()


if __name__ == "__main__":
    create_es_index()
