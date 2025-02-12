package com.bockerl.snailmember.file.query.service

import com.bockerl.snailmember.file.query.vo.request.QueryFileRequestVO
import com.bockerl.snailmember.file.query.vo.response.QueryFileGatheringResponseVO
import com.bockerl.snailmember.file.query.vo.response.QueryFileResponseVO

interface QueryFileService {
    fun readFilesByTarget(queryFileRequestVO: QueryFileRequestVO): List<QueryFileResponseVO>

    fun readFilesByGatheringId(gatheringId: Long): List<QueryFileGatheringResponseVO>
}