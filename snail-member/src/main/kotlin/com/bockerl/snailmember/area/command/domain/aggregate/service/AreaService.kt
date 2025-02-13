/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.area.command.domain.aggregate.service

import com.bockerl.snailmember.area.command.domain.aggregate.entity.EmdAreas
import com.bockerl.snailmember.area.command.domain.aggregate.entity.SidoAreas
import com.bockerl.snailmember.area.command.domain.aggregate.entity.SiggAreas
import com.bockerl.snailmember.area.command.domain.repository.EmdAreasRepository
import com.bockerl.snailmember.area.command.domain.repository.SidoAreasRepository
import com.bockerl.snailmember.area.command.domain.repository.SiggAreasRepository
import com.opencsv.CSVReader
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class LegalDistrictImportService(
    private val sidoRepository: SidoAreasRepository,
    private val siggRepository: SiggAreasRepository,
    private val emdRepository: EmdAreasRepository,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    // 행정구역 데이터를 담을 내부 클래스
    private data class DistrictData(
        val code: String, // 법정동코드
        val sidoName: String, // 시도명
        val siggName: String?, // 시군구명
        val emdName: String?, // 읍면동명
        val sequence: Int, // 순위
        val createdDate: LocalDate, // 생성일자
        val deletedDate: LocalDate?, // 삭제일자
    )

    // 파일을 읽어서 데이터를 처리하는 메인 메서드
    @Transactional
    fun importDistrictsFromStream(inputStream: InputStream) {
        // 데이터를 저장할 리스트 준비
        val districts = readCsvFile(inputStream)

        // 시도 데이터 먼저 저장하고 매핑 정보 생성
        val sidoMap = saveSidoData(districts)

        // 시군구 데이터 저장하고 매핑 정보 생성
        val siggMap = saveSiggData(districts, sidoMap)

        // 읍면동 데이터 저장
        saveEmdData(districts, siggMap)

        println("데이터 임포트가 완료되었습니다.")
    }

    // CSV 파일 읽기
    private fun readCsvFile(inputStream: InputStream): List<DistrictData> {
        val districts = mutableListOf<DistrictData>()

        InputStreamReader(inputStream, "EUC-KR").use { reader ->
            CSVReader(reader).use { csvReader ->
                // 헤더 건너뛰기
                csvReader.readNext()

                // 데이터 읽기
                while (true) {
                    csvReader
                        .readNext()
                        // let을 황용한 non null scope
                        ?.let { line ->
                            // 삭제된 데이터는 제외
                            if (line[7].isNotBlank()) return@let

                            districts.add(
                                DistrictData(
                                    code = line[0],
                                    sidoName = line[1],
                                    // 값이 있는지 엄격하게 확인
                                    siggName = line[2].takeIf { it.isNotBlank() } ?: "${line[1]} 미등록 행정구역",
                                    emdName = line[3].takeIf { it.isNotBlank() },
                                    sequence = line[5].toIntOrNull() ?: 0,
                                    createdDate = parseDateString(line[6]),
                                    deletedDate = line[7].takeIf { it.isNotBlank() }?.let { parseDateString(it) },
                                ),
                            )
                        } ?: break
                }
            }
        }
        return districts
    }

    // 시도 데이터 저장
    private fun saveSidoData(districts: List<DistrictData>): Map<String, Long?> {
        // nullable한 value를 가질 수 있도록 세팅(실제 넘어오는 값이 없을 수도 있고, 이렇게 하지 않으면 Elvis 연산자를 사용해야 합니다
        // sidoMap[district.code.subString(0, 2)] = sido.sidoAreasId ?: throw ~ 로 오히려 예외를 던져 스무스한 저장이 힙듭니다.
        val sidoMap = mutableMapOf<String, Long?>()

        districts
            .filter { it.code.substring(2) == "00000000" }
            .forEach { district ->
                val sido =
                    sidoRepository.save(
                        SidoAreas.create(
                            district.code.substring(0, 2),
                            district.sidoName,
                        ),
                    )
                sidoMap[district.code.substring(0, 2)] = sido.sidoAreasId
            }

        return sidoMap
    }

    // 시군구 데이터 저장
    private fun saveSiggData(
        districts: List<DistrictData>,
        sidoMap: Map<String, Long?>,
    ): Map<String, Long?> {
        val siggMap = mutableMapOf<String, Long?>()

        districts
            .filter { it.code.substring(5) == "00000" && it.code.substring(2) != "00000000" }
            .forEach { district ->
                log.info("처리 중인 시군구 데이터 : ${district.code}, 이름: ${district.sidoName}")
                val sidoId =
                    sidoMap[district.code.substring(0, 2)]
                        ?: throw IllegalStateException("시도 데이터가 없습니다: ${district.code}")
                val siggName =
                    district.siggName?.takeIf { it.isNotBlank() }
                        ?: "미상의 코드 ${district.code.substring(0, 5)}"
                val sigg =
                    siggRepository.save(
                        SiggAreas.create(
                            sidoId,
                            district.code.substring(0, 5),
                            district.siggName ?: "",
                        ),
                    )
                siggMap[district.code.substring(0, 5)] = sigg.siggAreasId
            }

        return siggMap
    }

    // 읍면동 데이터 저장
    private fun saveEmdData(
        districts: List<DistrictData>,
        siggMap: Map<String, Long?>,
    ) {
        val numPattern = Regex("\\d+")

        districts
            .filter { district ->
                // 시군구 레벨 데이터 필터링 조건 수정
                district.code.length == 10 &&
                    // 전체 코드 길이가 10자리
                    district.code.substring(2, 5).matches(numPattern) &&
                    // 시군구 코드가 숫자로 구성
                    district.code.substring(5) != "00000" &&
                    // 읍면동 부분이 00000
                    district.code.substring(2) != "00000000" // 시도 데이터가 아님
            }.forEach { district ->
                val siggId =
                    siggMap[district.code.substring(0, 5)]
                        ?: throw IllegalStateException("시군구 데이터가 없습니다: ${district.code}")

                val sigg =
                    emdRepository.save(
                        EmdAreas.create(
                            siggId,
                            district.code.substring(0, 10),
                            district.emdName ?: "",
                        ),
                    )
            }
    }

    private fun parseDateString(dateStr: String): LocalDate =
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE)
        } catch (e: Exception) {
            LocalDate.now()
        }
}

// 애플리케이션 시작 시 데이터 임포트를 실행하는 Runner
@Component
class DataImportRunner(
    private val importService: LegalDistrictImportService,
    private val sidoRepository: SidoAreasRepository,
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun run(args: ApplicationArguments) {
        if (sidoRepository.count() == 0L) {
            val resourcePath = "com/bockerl/snailmember/data/district20240802.csv"
            try {
                val resource = ClassPathResource(resourcePath)
                // 리소스가 실제로 존재하는지 확인
                if (!resource.exists()) {
                    log.error("리소스를 찾을 수 없습니다: $resourcePath")
                    return
                }

                // 스트림으로 직접 전달
                resource.inputStream.use { inputStream ->
                    importService.importDistrictsFromStream(inputStream)
                }

                log.info("법정동 데이터 임포트가 완료되었습니다.")
            } catch (e: Exception) {
                log.error("법정동 데이터 임포트 중 오류가 발생했습니다", e)
                throw e
            }
        }
    }
}