/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: Int,
    val httpStatus: HttpStatus,
    val message: String,
) {
    // 400: 잘못된 요청 (Bad Request)
    WRONG_ENTRY_POINT(40000, HttpStatus.BAD_REQUEST, "잘못된 접근입니다"),
    MISSING_REQUEST_PARAMETER(40001, HttpStatus.BAD_REQUEST, "필수 요청 파라미터가 누락되었습니다."),
    INVALID_PARAMETER_FORMAT(40002, HttpStatus.BAD_REQUEST, "요청에 유효하지 않은 인자 형식입니다."),
    BAD_REQUEST_JSON(40003, HttpStatus.BAD_REQUEST, "잘못된 JSON 형식입니다."),
    DATA_INTEGRITY_VIOLATION(40004, HttpStatus.BAD_REQUEST, "데이터 무결성 위반입니다. 필수 값이 누락되었거나 유효하지 않습니다."),
    INVALID_INPUT_VALUE(40005, HttpStatus.BAD_REQUEST, "잘못된 입력 값입니다."),
    INVALID_REQUEST_BODY(40006, HttpStatus.BAD_REQUEST, "잘못된 요청 본문입니다."),
    MISSING_REQUIRED_FIELD(40007, HttpStatus.BAD_REQUEST, "필수 필드가 누락되었습니다."),
    INVALID_COMMENT_TYPE(40008, HttpStatus.BAD_REQUEST, "올바르지 않은 댓글 타입입니다."),
    INVALID_RANDOM_QUESTION_STATUS(40012, HttpStatus.BAD_REQUEST, "답변이 완료된 상태가 아닙니다."),
    INACTIVE_REPLY(40013, HttpStatus.BAD_REQUEST, "해당 대댓글은 삭제돼 있습니다."),
    EMPTY_REQUEST_INPUTSTREAM(40015, HttpStatus.BAD_REQUEST, "요청 본문이 비어 있습니다. 필수 데이터를 포함해야 합니다."),
    TOO_MANY_FILES(40016, HttpStatus.BAD_REQUEST, "파일 업로드 제한 수를 넘었습니다."),
    ALREADY_LIKED(40017, HttpStatus.BAD_REQUEST, "이미 좋아요가 존재합니다"),
    ALREADY_UNLIKED(40018, HttpStatus.BAD_REQUEST, "이미 좋아요는 취소되었습니다"),

    // 401: 인증 실패 (Unauthorized)
    INVALID_HEADER_VALUE(40100, HttpStatus.UNAUTHORIZED, "올바르지 않은 헤더값입니다."),
    EXPIRED_TOKEN_ERROR(40101, HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_TOKEN_ERROR(40102, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_MALFORMED_ERROR(40103, HttpStatus.UNAUTHORIZED, "토큰이 올바르지 않습니다."),
    TOKEN_TYPE_ERROR(40104, HttpStatus.UNAUTHORIZED, "토큰 타입이 일치하지 않거나 비어있습니다."),
    TOKEN_UNSUPPORTED_ERROR(40105, HttpStatus.UNAUTHORIZED, "지원하지 않는 토큰입니다."),
    TOKEN_GENERATION_ERROR(40106, HttpStatus.UNAUTHORIZED, "토큰 생성에 실패하였습니다."),
    TOKEN_UNKNOWN_ERROR(40107, HttpStatus.UNAUTHORIZED, "알 수 없는 토큰입니다."),
    LOGIN_FAILURE(40108, HttpStatus.UNAUTHORIZED, "로그인에 실패했습니다"),
    UNAUTHORIZED_ACCESS(40109, HttpStatus.UNAUTHORIZED, "인증되지 않은 접근입니다."),
    EXPIRED_SIGNUP_SESSION(40110, HttpStatus.UNAUTHORIZED, "회원가입 세션이 만료되었습니다."),
    EXIST_USER(401121, HttpStatus.UNAUTHORIZED, "이미 회원가입한 회원입니다."),
    NOT_FOUND_USER_ID(40112, HttpStatus.UNAUTHORIZED, "아이디를 잘못 입력하셨습니다."),
    INVALID_PASSWORD(40113, HttpStatus.UNAUTHORIZED, "비밀번호를 잘못 입력하셨습니다."),
    EXPIRED_CODE(40114, HttpStatus.UNAUTHORIZED, "만료된 코드입니다."),
    INVALID_AUTHENTICATION_OBJECT(40115, HttpStatus.UNAUTHORIZED, "Authentication 객체가 CustomUserDetails 타입이 아닙니다."),
    INVALID_CODE(40116, HttpStatus.UNAUTHORIZED, "유효하지 않은 코드입니다."),

    // 403: 권한 부족 (Forbidden)
    FORBIDDEN_ROLE(40300, HttpStatus.FORBIDDEN, "요청한 리소스에 대한 권한이 없습니다."),
    ACCESS_DENIED(40301, HttpStatus.FORBIDDEN, "접근 권한이 거부되었습니다."),

    // 404: 리소스를 찾을 수 없음 (Not Found)
    NOT_FOUND_MEMBER(40401, HttpStatus.NOT_FOUND, "회원이 존재하지 않습니다."),
    NOT_FOUND_SIDO(40402, HttpStatus.NOT_FOUND, "존재하지 않는 시도(Sido)입니다."),
    NOT_FOUND_SIGG(40403, HttpStatus.NOT_FOUND, "존재하지 않는 시군구(Sigg)입니다."),
    NOT_FOUND_EMD(40404, HttpStatus.NOT_FOUND, "존재하지 않는 읍면동(Emd)입니다."),
    NOT_FOUND_COMMENT(40405, HttpStatus.NOT_FOUND, "댓글이 존재하지 않습니다."),
    NOT_FOUND_INQUIRY(40408, HttpStatus.NOT_FOUND, "문의가 존재하지 않습니다."),
    NOT_FOUND_QUES_ANSWER(40411, HttpStatus.NOT_FOUND, "답변이 존재하지 않습니다."),
    NOT_FOUND_REPLY(40414, HttpStatus.NOT_FOUND, "해당 대댓글이 존재하지 않습니다"),
    NOT_FOUND_BOARD(40415, HttpStatus.NOT_FOUND, "해당 게시글이 존재하지 않습니다"),
    NOT_FOUND_FILE(40415, HttpStatus.NOT_FOUND, "해당 파일이 존재하지 않습니다"),
    NOT_FOUND_BOARD_COMMENT(40415, HttpStatus.NOT_FOUND, "해당 게시글 댓글이 존재하지 않습니다"),

    // 429: 요청 과다 (Too Many Requests)
    TOO_MANY_REQUESTS(42900, HttpStatus.TOO_MANY_REQUESTS, "요청 횟수가 너무 많습니다. 잠시 후 다시 시도해 주세요."),

    // 500: 서버 내부 오류 (Internal Server Error)
    INTERNAL_SERVER_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다"),
    MAIL_SEND_FAIL(50001, HttpStatus.INTERNAL_SERVER_ERROR, "인증 메일 전송에 실패했습니다."),
    ;

    companion object {
        // 에러 코드로 ErrorCode를 찾는 확장 함수
        fun findByCode(code: Int): ErrorCode =
            entries.find { it.code == code }
                ?: throw IllegalArgumentException("Invalid error code: $code")
    }
}