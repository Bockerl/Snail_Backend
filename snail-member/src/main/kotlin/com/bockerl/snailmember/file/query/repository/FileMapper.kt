package com.bockerl.snailmember.file.query.repository

import com.bockerl.snailmember.file.query.dto.QueryFileDTO
import com.bockerl.snailmember.file.query.dto.QueryFileGatheringDTO
import com.bockerl.snailmember.file.query.vo.request.QueryFileRequestVO
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface FileMapper {
    fun selectFilesByFileTarget(@Param("queryFileRequestVO") queryFileRequestVO: QueryFileRequestVO): List<QueryFileDTO>

    fun selectFilesByGatheringId(@Param("gatheringId") gatheringId: Long): List<QueryFileGatheringDTO>
}
