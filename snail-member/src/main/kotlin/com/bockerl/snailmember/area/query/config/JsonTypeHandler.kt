package com.bockerl.snailmember.area.query.config

import com.bockerl.snailmember.area.command.domain.aggregate.entity.EmdArea
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

class JsonTypeHandler(private val objectMapper: ObjectMapper) : BaseTypeHandler<List<EmdArea.ReeArea>>() {
    private val logger = KotlinLogging.logger {}

    // DB에 데이터를 저장할 때 호출됨
    override fun setNonNullParameter(
        ps: PreparedStatement,
        i: Int,
        parameter: List<EmdArea.ReeArea>,
        jdbcType: JdbcType?,
    ) {
        ps.setObject(i, objectMapper.writeValueAsString(parameter), Types.OTHER)
    }

    // 컬럼명으로 결과를 조회할 때 호출됨
    override fun getNullableResult(rs: ResultSet, columnName: String): List<EmdArea.ReeArea>? =
        rs.getString(columnName)?.let {
            convertToList(it)
        }

    // 컬럼 인덱스로 결과를 조회할 때 호출됨
    override fun getNullableResult(rs: ResultSet, columnIndex: Int): List<EmdArea.ReeArea>? =
        rs.getString(columnIndex)?.let {
            convertToList(it)
        }

    // 저장 프로시저의 결과를 조회할 때 호출됨
    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): List<EmdArea.ReeArea>? =
        cs.getString(columnIndex)?.let { convertToList(it) }

    private fun convertToList(jsonString: String): List<EmdArea.ReeArea> {
        if (jsonString.isBlank()) {
            return emptyList()
        }

        return try {
            objectMapper.readValue(
                jsonString,
                objectMapper.typeFactory.constructCollectionType(
                    List::class.java,
                    EmdArea.ReeArea::class.java,
                ),
            )
        } catch (e: Exception) {
            logger.error { "JSON 문자열을 List로 변환하는데 실패했습니다: ${e.message}" }
            emptyList()
        }
    }
}
