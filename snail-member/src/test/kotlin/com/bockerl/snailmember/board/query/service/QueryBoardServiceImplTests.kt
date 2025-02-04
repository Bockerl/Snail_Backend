package com.bockerl.snailmember.board.query.service

import com.bockerl.snailmember.board.command.application.dto.BoardDTO
import com.bockerl.snailmember.board.command.application.mapper.BoardConverter
import com.bockerl.snailmember.board.command.domain.aggregate.entity.Board
import com.bockerl.snailmember.board.command.domain.aggregate.entity.BoardTag
import com.bockerl.snailmember.board.command.domain.aggregate.entity.BoardType
import com.bockerl.snailmember.board.query.repository.BoardMapper
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.LocalDateTime

@SpringBootTest
class QueryBoardServiceImplTests {

    @Autowired
    private lateinit var queryBoardServiceImpl: QueryBoardServiceImpl

    @MockBean
    private lateinit var boardMapper: BoardMapper

    @MockBean
    private lateinit var boardConverter: BoardConverter

@Test
 fun `게시글 pk로 게시판 상세 조회 성공 테스트`() {
    /* given */
    val boardId = 1L
    val boardIdResult = "BOA-00000001"

    // boardMapper가 boardId로 조회하면 더미 데이터를 반환하도록 설정
    val boardEntity = Board(boardId, "자유 게시판에 오신 것을 환영합니다!", BoardType.FREE,
        BoardTag.TIP, "종로 2가", "전체", 10, true, 1)
    given(boardMapper.selectBoardByBoardId(boardId)).willReturn(boardEntity)

    // boardConverter가 BoardEntity를 BoardDTO로 변환하도록 설정
    val boardDTO = BoardDTO("BOA-00000001", "자유 게시판에 오신 것을 환영합니다!", BoardType.FREE,
        BoardTag.TIP, "종로 2가", "전체", 10, true, 1)
    given(boardConverter.entityToDTO(boardEntity)).willReturn(boardDTO)


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
    val resultBoardType = BoardType.FREE

    // boardMapper가 boardId로 조회하면 더미 데이터를 반환하도록 설정
    val boardEntityList = listOf(Board(1L, "자유 게시판에 오신 것을 환영합니다!", BoardType.FREE,
        BoardTag.TIP, "종로 2가", "전체", 10, true, 1))
    given(boardMapper.selectBoardByBoardType(boardType)).willReturn(boardEntityList)

    // boardConverter가 BoardEntity를 BoardDTO로 변환하도록 설정
    val boardDTOList = listOf(BoardDTO("BOA-00000001", "자유 게시판에 오신 것을 환영합니다!", BoardType.FREE,
        BoardTag.TIP, "종로 2가", "전체", 10, true, 1))
    boardEntityList.forEachIndexed { index, board ->
        given(boardConverter.entityToDTO(board)).willReturn(boardDTOList[index])
    }

    /* when */
    val result = queryBoardServiceImpl.readBoardByBoardType(boardType)

    /* then */
    then(result).isNotEmpty()
    then(result.size).isEqualTo(boardDTOList.size)
    then(result[0].boardType).isEqualTo(resultBoardType)
}

    @Test
    fun `게시글 타입으로 게시글 List 조회 실패 테스트`() {
        /* given */
        val boardType = "FREE"
        val resultBoardType = BoardType.FREE

        // boardMapper가 boardId로 조회하면 더미 데이터를 반환하도록 설정
        val boardEntityList = listOf(Board(1L, "자유 게시판에 오신 것을 환영합니다!", BoardType.POLICY,
            BoardTag.TIP, "종로 2가", "전체", 10, true, 1))
        given(boardMapper.selectBoardByBoardType(boardType)).willReturn(boardEntityList)

        // boardConverter가 BoardEntity를 BoardDTO로 변환하도록 설정
        val boardDTOList = listOf(BoardDTO("BOA-00000001", "자유 게시판에 오신 것을 환영합니다!", BoardType.POLICY,
            BoardTag.TIP, "종로 2가", "전체", 10, true, 1))
        boardEntityList.forEachIndexed { index, board ->
            given(boardConverter.entityToDTO(board)).willReturn(boardDTOList[index])
        }

        /* when */
        val result = queryBoardServiceImpl.readBoardByBoardType(boardType)

        /* then */
        then(result).isNotEmpty()
        then(result.size).isEqualTo(boardDTOList.size)
        then(result[0].boardType).isEqualTo(resultBoardType)
    }

    @Test
    fun `게시글 태그로 게시글 List 조회 성공 테스트`() {
        /* given */
        val boardTag = listOf("TIP")
        val resultBoardTag = BoardTag.TIP

        // boardMapper가 boardId로 조회하면 더미 데이터를 반환하도록 설정
        val boardEntityList = listOf(Board(1L, "자유 게시판에 오신 것을 환영합니다!", BoardType.FREE,
            BoardTag.TIP, "종로 2가", "전체", 10, true, 1))
        given(boardMapper.selectBoardByBoardTag(boardTag)).willReturn(boardEntityList)

        // boardConverter가 BoardEntity를 BoardDTO로 변환하도록 설정
        val boardDTOList = listOf(BoardDTO("BOA-00000001", "자유 게시판에 오신 것을 환영합니다!", BoardType.FREE,
            BoardTag.TIP, "종로 2가", "전체", 10, true, 1))
        boardEntityList.forEachIndexed { index, board ->
            given(boardConverter.entityToDTO(board)).willReturn(boardDTOList[index])
        }

        /* when */
        val result = queryBoardServiceImpl.readBoardByBoardTag(boardTag)

        /* then */
        then(result).isNotEmpty()
        then(result.size).isEqualTo(boardDTOList.size)
        then(result[0].boardTag).isEqualTo(resultBoardTag)
    }

    @Test
    fun `게시글 태그로 게시글 List 조회 실패 테스트`() {
        /* given */
        val boardTag = listOf("TIP")
        val resultBoardTag = BoardTag.VISA

        // boardMapper가 boardId로 조회하면 더미 데이터를 반환하도록 설정
        val boardEntityList = listOf(Board(1L, "자유 게시판에 오신 것을 환영합니다!", BoardType.POLICY,
            BoardTag.TIP, "종로 2가", "전체", 10, true, 1))
        given(boardMapper.selectBoardByBoardTag(boardTag)).willReturn(boardEntityList)

        // boardConverter가 BoardEntity를 BoardDTO로 변환하도록 설정
        val boardDTOList = listOf(BoardDTO("BOA-00000001", "자유 게시판에 오신 것을 환영합니다!", BoardType.POLICY,
            BoardTag.TIP, "종로 2가", "전체", 10, true, 1))
        boardEntityList.forEachIndexed { index, board ->
            given(boardConverter.entityToDTO(board)).willReturn(boardDTOList[index])
        }

        /* when */
        val result = queryBoardServiceImpl.readBoardByBoardTag(boardTag)

        /* then */
        then(result).isNotEmpty()
        then(result.size).isEqualTo(boardDTOList.size)
        then(result[0].boardTag).isEqualTo(resultBoardTag)
    }

}