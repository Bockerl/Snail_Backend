package com.bockerl.snailchat.chat.query.repository.queryUtil

import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

@Component
class MongoQueryUtil(
    private val mongoTemplate: MongoTemplate,
) {
    /**
     * 특정 컬렉션에서 페이징 조회
     * @param collection 조회할 컬렉션의 클래스 타입
     * @param criteria 검색 조건
     * @param sortField 정렬 기준 필드
     * @param pageSize 페이지 크기
     * @return 페이징된 결과 리스트
     */
    fun <T> findWithPaging(
        collection: Class<T>,
        criteria: Criteria,
        sortField: String,
        pageSize: Int,
    ): List<T> {
        val query =
            Query
                .query(criteria)
                .with(Sort.by(Sort.Direction.DESC, sortField))
                .limit(pageSize) // 페이지 크기 제한

        return mongoTemplate.find(query, collection)
    }

    fun <T> findWithPagingSkip(
        collection: Class<T>,
        criteria: Criteria,
        sortField: String,
        pageSize: Int,
        skip: Int,
        sortDirection: Sort.Direction,
    ): List<T> {
        val query =
            Query
                .query(criteria)
                .with(Sort.by(sortDirection, sortField))
                .skip(skip.toLong())
                .limit(pageSize)

        return mongoTemplate.find(query, collection)
    }

    fun <T> findWithLimitAndSort(
        collection: Class<T>,
        criteria: Criteria,
        sortField: String,
        limit: Int,
        sortDirection: Sort.Direction = Sort.Direction.DESC,
    ): List<T> {
        val query =
            Query
                .query(criteria)
                .with(Sort.by(sortDirection, sortField))
                .limit(limit)

        return mongoTemplate.find(query, collection)
    }

    /**
     * 특정 컬렉션에서 정렬된 데이터 조회
     * @param collection 조회할 컬렉션의 클래스 타입
     * @param criteria 검색 조건
     * @param sortField 정렬 기준 필드
     * @param sortDirection 정렬 방향 (오름차순/내림차순)
     * @return 정렬된 결과 리스트
     */
    fun <T> findSorted(
        collection: Class<T>,
        criteria: Criteria,
        sortField: String,
        sortDirection: Sort.Direction,
    ): List<T> {
        val query =
            Query
                .query(criteria)
                .with(Sort.by(sortDirection, sortField))
        return mongoTemplate.find(query, collection)
    }

    /**
     * 특정 조건에 맞는 문서 개수 조회
     * @param collection 조회할 컬렉션의 클래스 타입
     * @param criteria 검색 조건
     * @return 문서 개수
     */
    fun <T> countDocuments(
        collection: Class<T>,
        criteria: Criteria,
    ): Long {
        val query = Query.query(criteria)
        return mongoTemplate.count(query, collection)
    }
}