package com.bockerl.snailmember.area.command.domain.aggregate.service

import com.bockerl.snailmember.area.command.domain.aggregate.entity.EmdAreas
import com.bockerl.snailmember.area.command.domain.aggregate.entity.SidoAreas
import com.bockerl.snailmember.area.command.domain.aggregate.entity.SiggAreas
import com.bockerl.snailmember.area.command.domain.repository.EmdAreasRepository
import com.bockerl.snailmember.area.command.domain.repository.SidoAreasRepository
import com.bockerl.snailmember.area.command.domain.repository.SiggAreasRepository
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

@Service
class AreaApiService(
    private val sidoAreasRepository: SidoAreasRepository,
    private val siggAreasRepository: SiggAreasRepository,
    private val emdAreasRepository: EmdAreasRepository,
    private val objectMapper: ObjectMapper,
    @Value("\${AREA_API_KEY}")
    private val key: String,
) {
    private val logger = KotlinLogging.logger {}

    // 데이터베이스가 비어있는지 확인하는 메서드
    fun isDatabaseEmpty(): Boolean = sidoAreasRepository.count() == 0L

    // 메인 데이터 수집 메서드
    @Transactional
    fun fetchApi() {
        try {
            logger.info { "지역 데이터 수집을 시작합니다." }

            // 시도 데이터 처리
            val sidoJsonResponse = loadSidoAreas()
            val sidoList = parseSidoResponseArea(sidoJsonResponse)
            saveSidoAreas(sidoList)
            logger.info { "시도 데이터 저장 완료: ${sidoList.size}개" }

            // 시군구 데이터 처리
            val siggList = loadSiggAreas(sidoList)
            saveSiggAreas(siggList)
            logger.info { "시군구 데이터 저장 완료: ${siggList.size}개" }

            // 읍면동 및 리 데이터 처리
            val emdList = loadEmdAreas(siggList)
            processReeDataForEmdAreas(emdList)
            saveEmdAreas(emdList)
            logger.info { "읍면동 및 리 데이터 저장 완료: ${emdList.size}개" }

            logger.info { "모든 지역 데이터 수집이 완료되었습니다." }
        } catch (e: Exception) {
            logger.error(e) { "지역 데이터 수집 중 오류 발생: ${e.message}" }
            throw e
        }
    }

    private fun loadSidoAreas(): String =
        executeApiCall(
            baseUrl = "https://api.vworld.kr/ned/data/admCodeList",
            parameters =
                mapOf(
                    "key" to key,
                    "format" to "json",
                    "numOfRows" to "20",
                    "pageNo" to "1",
                    "domain" to "",
                ),
            logPrefix = "시도",
        )

    private fun loadSiggAreas(sidoList: List<SidoAreas>): List<SiggAreas> =
        sidoList.flatMap { sidoArea ->
            val siggJsonResponse = loadSiggAreasForSido(sidoArea)
            parseSiggResponseArea(siggJsonResponse, sidoArea)
        }

    private fun loadSiggAreasForSido(sidoArea: SidoAreas): String =
        executeApiCall(
            baseUrl = "https://api.vworld.kr/ned/data/admSiList",
            parameters =
                mapOf(
                    "key" to key,
                    "format" to "json",
                    "numOfRows" to "100",
                    "pageNo" to "1",
                    "admCode" to sidoArea.sidoAreaAdmCode,
                    "domain" to "",
                ),
            logPrefix = "시군구[${sidoArea.sidoAreaName}]",
        )

    private fun loadEmdAreas(siggList: List<SiggAreas>): List<EmdAreas> =
        siggList.flatMap { siggArea ->
            val emdJsonResponse = loadEmdAreasForSigg(siggArea)
            parseEmdResponseArea(emdJsonResponse, siggArea)
        }

    private fun loadEmdAreasForSigg(siggArea: SiggAreas): String =
        executeApiCall(
            baseUrl = "https://api.vworld.kr/ned/data/admDongList",
            parameters =
                mapOf(
                    "key" to key,
                    "format" to "json",
                    "numOfRows" to "200",
                    "pageNo" to "1",
                    "admCode" to siggArea.siggAreaAdmCode,
                    "domain" to "",
                ),
            logPrefix = "읍면동[${siggArea.siggAreaName}]",
        )

    // 리 데이터 처리를 위한 메서드
    private fun processReeDataForEmdAreas(emdList: List<EmdAreas>) {
        emdList.forEach { emd ->
            try {
                val reeJsonResponse =
                    executeApiCall(
                        baseUrl = "https://api.vworld.kr/ned/data/admReeList",
                        parameters =
                            mapOf(
                                "key" to key,
                                "format" to "json",
                                "numOfRows" to "100",
                                "pageNo" to "1",
                                "admCode" to emd.emdAreaAdmCode,
                                "domain" to "",
                            ),
                        logPrefix = "리[${emd.emdAreaName}]",
                    )

                val reeList = parseReeResponseArea(reeJsonResponse, emd)
                if (reeList.isEmpty()) {
                    logger.info { "읍면동 '${emd.emdAreaName}'에는 리 데이터가 없습니다." }
                    emd.ReeAreas = emptyList()
                    return@forEach
                }

                emd.ReeAreas = reeList
                logger.info { "읍면동 '${emd.emdAreaName}'의 리 데이터 처리 완료: ${reeList.size}개" }
            } catch (e: Exception) {
                logger.error { "리 데이터 처리 중 에러 발생: (emdAreaName: ${emd.emdAreaName}): ${e.message}" }
                emd.ReeAreas = emptyList()
            }
        }
    }

    private fun executeApiCall(
        baseUrl: String,
        parameters: Map<String, String>,
        logPrefix: String,
    ): String {
        val urlBuilder = StringBuilder(baseUrl)
        val parameterBuilder = StringBuilder()

        parameters.forEach { (key, value) ->
            val encodedKey = URLEncoder.encode(key, "UTF-8")
            val encodedValue = URLEncoder.encode(value, "UTF-8")
            if (parameterBuilder.isEmpty()) {
                parameterBuilder.append("?$encodedKey=$encodedValue")
            } else {
                parameterBuilder.append("&$encodedKey=$encodedValue")
            }
        }

        val url = URL(urlBuilder.toString() + parameterBuilder.toString())
        logger.info { "$logPrefix 요청 URL: $url" }

        val conn =
            (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Content-Type", "application/json")
            }

        logger.info { "$logPrefix response Code: ${conn.responseCode}" }

        return if (conn.responseCode in 200..300) {
            BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                reader.readText()
            }
        } else {
            logger.error { "$logPrefix API 요청 에러 시 발생하는 오류" }
            throw CommonException(ErrorCode.BAD_REQUEST_JSON)
        }.also { conn.disconnect() }
    }

    private fun parseSidoResponseArea(sidoJsonResponse: String): List<SidoAreas> {
        data class AdmVO(
            val admCode: String,
            val admCodeNm: String,
            val lowestAdmCodeNm: String,
        )

        data class AdmVOListWrapper(
            val pageNo: String,
            val admVOList: List<AdmVO>,
            val totalCount: String,
            val numOfRows: String,
        )

        data class ApiResponse(
            val admVOList: AdmVOListWrapper,
        )

        val response = objectMapper.readValue<ApiResponse>(sidoJsonResponse)

        return response.admVOList.admVOList.map { admVO ->
            SidoAreas.create(
                admCode = admVO.admCode,
                name = admVO.lowestAdmCodeNm,
            )
        }
    }

    private fun parseSiggResponseArea(
        siggJsonResponse: String,
        sidoArea: SidoAreas,
    ): List<SiggAreas> {
        data class AdmVO(
            val admCode: String,
            val admCodeNm: String,
            val lowestAdmCodeNm: String,
        )

        data class AdmVOListWrapper(
            val pageNo: String,
            val admVOList: List<AdmVO>,
            val totalCount: String,
            val numOfRows: String,
        )

        data class ApiResponse(
            val admVOList: AdmVOListWrapper,
        )

        val response = objectMapper.readValue<ApiResponse>(siggJsonResponse)

        return response.admVOList.admVOList.map { siggAdmVO ->
            SiggAreas.create(
                admCode = siggAdmVO.admCode,
                areaName = siggAdmVO.lowestAdmCodeNm,
                sidoAreaId = sidoArea.sidoAreaId,
                fullName = siggAdmVO.admCodeNm,
            )
        }
    }

    private fun parseEmdResponseArea(
        emdJsonResponse: String,
        siggArea: SiggAreas,
    ): List<EmdAreas> {
        data class AdmVO(
            val admCode: String,
            val admCodeNm: String,
            val lowestAdmCodeNm: String,
        )

        data class AdmVOListWrapper(
            val pageNo: String,
            val admVOList: List<AdmVO>,
            val totalCount: String,
            val numOfRows: String,
        )

        data class ApiResponse(
            val admVOList: AdmVOListWrapper,
        )

        val response = objectMapper.readValue<ApiResponse>(emdJsonResponse)

        return response.admVOList.admVOList.map { emdAdmVO ->
            EmdAreas.create(
                admCode = emdAdmVO.admCode,
                areaName = emdAdmVO.lowestAdmCodeNm,
                siggAreaId = siggArea.siggAreaId,
                fullName = emdAdmVO.admCodeNm,
            )
        }
    }

    private fun parseReeResponseArea(
        reeJsonResponse: String,
        emdArea: EmdAreas,
    ): List<EmdAreas.ReeArea> {
        data class AdmVO(
            val admCode: String,
            val admCodeNm: String,
            val lowestAdmCodeNm: String,
        )

        data class AdmVOListWrapper(
            val pageNo: String,
            val admVOList: List<AdmVO>? = null,
            val totalCount: String,
            val error: String = "",
            val message: String = "",
            val numOfRows: String,
        )

        data class ApiResponseWithAdmVOList(
            val admVOList: AdmVOListWrapper,
        )

        data class ApiResponseEmpty(
            val response: AdmVOListWrapper,
        )

        return try {
            // 먼저 리가 있는 경우의 응답 구조로 파싱 시도
            val responseWithRee =
                runCatching {
                    objectMapper.readValue<ApiResponseWithAdmVOList>(reeJsonResponse)
                }.getOrNull()

            if (responseWithRee != null && responseWithRee.admVOList.admVOList != null) {
                return responseWithRee.admVOList.admVOList.map { reeAdmVO ->
                    EmdAreas.ReeArea(
                        reeAreaAdmCode = reeAdmVO.admCode,
                        reeAreasName = reeAdmVO.lowestAdmCodeNm,
                        fullName = reeAdmVO.admCodeNm,
                    )
                }
            }

            // 리가 없는 경우의 응답 구조로 파싱
            val responseEmpty = objectMapper.readValue<ApiResponseEmpty>(reeJsonResponse)
            if (responseEmpty.response.totalCount == "0") {
                logger.info { "읍면동 '${emdArea.emdAreaName}'에는 리 데이터가 없습니다." }
                return emptyList()
            }

            emptyList()
        } catch (e: Exception) {
            logger.error(e) { "리 데이터 파싱 중 에러 발생 (emdAreaName: ${emdArea.emdAreaName}): ${e.message}" }
            emptyList()
        }
    }

    private fun saveSidoAreas(sidoList: List<SidoAreas>) {
        sidoAreasRepository.saveAll(sidoList)
    }

    private fun saveSiggAreas(siggList: List<SiggAreas>) {
        siggAreasRepository.saveAll(siggList)
    }

    private fun saveEmdAreas(emdList: List<EmdAreas>) {
        emdAreasRepository.saveAll(emdList)
    }
}