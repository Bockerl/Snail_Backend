package com.bockerl.snailmember.board.query.service

import com.bockerl.snailmember.board.query.dto.QueryBoardDTO
import com.bockerl.snailmember.board.query.enums.QueryBoardTag
import com.bockerl.snailmember.board.query.enums.QueryBoardType
import com.bockerl.snailmember.board.query.mapper.QueryBoardConverter
import com.bockerl.snailmember.board.query.repository.BoardMapper
import com.bockerl.snailmember.board.query.vo.QueryBoardResponseVO
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
class QueryBoardServiceImplTests {

    @Autowired
    private lateinit var queryBoardServiceImpl: QueryBoardServiceImpl

    @MockBean
    private lateinit var boardMapper: BoardMapper

    @MockBean
    private lateinit var boardConverter: QueryBoardConverter

@Test
 fun `게시글 pk로 게시판 상세 조회 성공 테스트`() {
    /* given */
    val boardId = 1L
    val boardIdResult = "BOA-00000001"

    val queryBoardDTO = QueryBoardDTO(boardId, "자유 게시판에 오신 것을 환영합니다!", QueryBoardType.FREE,
        QueryBoardTag.TIP, "종로 2가", "전체", 10, true, 1)
    given(boardMapper.selectBoardByBoardId(boardId)).willReturn(queryBoardDTO)

    val queryBoardResponseVO = QueryBoardResponseVO("BOA-00000001", "자유 게시판에 오신 것을 환영합니다!", QueryBoardType.FREE,
        QueryBoardTag.TIP, "종로 2가", "전체", 10, true, 1)
    given(boardConverter.dtoToResponseVO(queryBoardDTO)).willReturn(queryBoardResponseVO)


    /* when */
    val result = queryBoardServiceImpl.readBoardByBoardId(boardId)

    /* then */
    then(result.boardId ?:"").isEqualTo(boardIdResult)
 }

@Test
fun `게시글 pk로 게시판 상세 조회 실패 테스트`() {
    /* given */
    val boardId = 1L
    val boardIdResult = "BOA-00000001"

    /* when */
    val result = queryBoardServiceImpl.readBoardByBoardId(boardId)

    /* then */
    then(result.boardId ?:"").isEqualTo(boardIdResult)
}

@Test
 fun `게시글 타입으로 게시글 List 조회 성공 테스트`() {
    /* given */
    val boardType = "FREE"
    val resultQueryBoardType = QueryBoardType.FREE

    // boardMapper가 boardId로 조회하면 더미 데이터를 반환하도록 설정
    val queryBoardDTOList = listOf(QueryBoardDTO(1L, "자유 게시판에 오신 것을 환영합니다!", QueryBoardType.FREE,
        QueryBoardTag.TIP, "종로 2가", "전체", 10, true, 1))
    given(boardMapper.selectBoardByBoardType(boardType)).willReturn(queryBoardDTOList)

    /* dto를 변환하며 vo로 유효성 검사 및 변환*/
    val queryBoardResponseVOList = listOf(
        QueryBoardResponseVO("BOA-00000001", "자유 게시판에 오신 것을 환영합니다!", QueryBoardType.FREE,
        QueryBoardTag.TIP, "종로 2가", "전체", 10, true, 1)
    )
    queryBoardDTOList.forEachIndexed { index, boardDTO ->
        given(boardConverter.dtoToResponseVO(boardDTO)).willReturn(queryBoardResponseVOList[index])
    }

    /* when */
    val result = queryBoardServiceImpl.readBoardByBoardType(boardType)

    /* then */
    then(result).isNotEmpty()
    then(result.size).isEqualTo(queryBoardDTOList.size)
    then(result[0].queryBoardType).isEqualTo(resultQueryBoardType)
}

    @Test
    fun `게시글 타입으로 게시글 List 조회 실패 테스트`() {
        /* given */
        val boardType = "FREE"
        val resultQueryBoardType = QueryBoardType.FREE

        val queryBoardDTOList = listOf(QueryBoardDTO(1L, "자유 게시판에 오신 것을 환영합니다!", QueryBoardType.POLICY,
            QueryBoardTag.TIP, "종로 2가", "전체", 10, true, 1))
        given(boardMapper.selectBoardByBoardType(boardType)).willReturn(queryBoardDTOList)

        val queryBoardResponseVOList = listOf(
            QueryBoardResponseVO("BOA-00000001", "자유 게시판에 오신 것을 환영합니다!", QueryBoardType.POLICY,
            QueryBoardTag.TIP, "종로 2가", "전체", 10, true, 1)
        )
        queryBoardDTOList.forEachIndexed { index, boardDTO ->
            given(boardConverter.dtoToResponseVO(boardDTO)).willReturn(queryBoardResponseVOList[index])
        }

        /* when */
        val result = queryBoardServiceImpl.readBoardByBoardType(boardType)

        /* then */
        then(result).isNotEmpty()
        then(result.size).isEqualTo(queryBoardDTOList.size)
        then(result[0].queryBoardType).isEqualTo(resultQueryBoardType)
    }

    @Test
    fun `게시글 태그로 게시글 List 조회 성공 테스트`() {
        /* given */
        val boardTag = listOf("TIP")
        val resultQueryBoardTag = QueryBoardTag.TIP

        val queryBoardDTOList = listOf(QueryBoardDTO(1L, "자유 게시판에 오신 것을 환영합니다!", QueryBoardType.FREE,
            QueryBoardTag.TIP, "종로 2가", "전체", 10, true, 1))
        given(boardMapper.selectBoardByBoardTag(boardTag)).willReturn(queryBoardDTOList)

        val queryBoardResponseVOList = listOf(
            QueryBoardResponseVO("BOA-00000001", "자유 게시판에 오신 것을 환영합니다!", QueryBoardType.FREE,
            QueryBoardTag.TIP, "종로 2가", "전체", 10, true, 1)
        )
        queryBoardDTOList.forEachIndexed { index, boardDTO ->
            given(boardConverter.dtoToResponseVO(boardDTO)).willReturn(queryBoardResponseVOList[index])
        }

        /* when */
        val result = queryBoardServiceImpl.readBoardByBoardTag(boardTag)

        /* then */
        then(result).isNotEmpty()
        then(result.size).isEqualTo(queryBoardDTOList.size)
        then(result[0].queryBoardTag).isEqualTo(resultQueryBoardTag)
    }

    @Test
    fun `게시글 태그로 게시글 List 조회 실패 테스트`() {
        /* given */
        val boardTag = listOf("TIP")
        val resultQueryBoardTag = QueryBoardTag.VISA

        val queryBoardDTOList = listOf(QueryBoardDTO(1L, "자유 게시판에 오신 것을 환영합니다!", QueryBoardType.FREE,
            QueryBoardTag.TIP, "종로 2가", "전체", 10, true, 1))
        given(boardMapper.selectBoardByBoardTag(boardTag)).willReturn(queryBoardDTOList)

        val queryBoardResponseVOList = listOf(
            QueryBoardResponseVO("BOA-00000001", "자유 게시판에 오신 것을 환영합니다!", QueryBoardType.FREE,
                QueryBoardTag.TIP, "종로 2가", "전체", 10, true, 1)
        )
        queryBoardDTOList.forEachIndexed { index, boardDTO ->
            given(boardConverter.dtoToResponseVO(boardDTO)).willReturn(queryBoardResponseVOList[index])
        }

        /* when */
        val result = queryBoardServiceImpl.readBoardByBoardTag(boardTag)

        /* then */
        then(result).isNotEmpty()
        then(result.size).isEqualTo(queryBoardDTOList.size)
        then(result[0].queryBoardTag).isEqualTo(resultQueryBoardTag)
    }

}