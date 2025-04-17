@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.gathering.command.application.controller

import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.gathering.command.application.mapper.GatheringConverter
import com.bockerl.snailmember.gathering.command.application.service.CommandGatheringService
import com.bockerl.snailmember.gathering.command.domain.aggregate.vo.request.*
import com.bockerl.snailmember.infrastructure.config.OpenApiBody
import com.bockerl.snailmember.security.config.CurrentMemberId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Encoding
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/gathering")
class CommandGatheringController(
    private val commandGatheringService: CommandGatheringService,
    private val gatheringConverter: GatheringConverter,
) {
    @Operation(
        summary = "모임 등록",
        description =
            """
            입력된 모임 정보와 파일을 바탕으로 모임을 등록합니다.
            모임 사진은 선택 사항으로 넣지 않아도 됩니다.
        """,
    )
    // 설명. api response 스키마 바꾸겠습니다.
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "모임 등록 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
                // 궁금. cud response는 schema 설정을 다르게 해줘야 하지 않을까..
            ),
        ],
    )
    @OpenApiBody(
        content = [
            Content(
                encoding = [
                    Encoding(
                        name = "commandGatheringCreateRequestVO",
                        contentType = MediaType.APPLICATION_JSON_VALUE,
                    ),
                ],
            ),
        ],
    )
    @PostMapping("", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postGathering(
        @RequestHeader("idempotencyKey") idempotencyKey: String,
        @RequestPart("commandGatheringCreateRequestVO") commandGatheringCreateRequestVO: CommandGatheringCreateRequestVO,
        @RequestPart("files", required = false) files: List<MultipartFile>?,
    ): ResponseDTO<*> {
        val commandGatheringCreateDTO = gatheringConverter.createRequestVOToDTO(commandGatheringCreateRequestVO, idempotencyKey)

        commandGatheringService.createGathering(commandGatheringCreateDTO, files ?: emptyList())

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "모임 수정",
        description =
            """
            입력된 모임 정보와 파일을 바탕으로 모임을 수정합니다.
            모임 사진은 선택 사항으로 넣지 않아도 됩니다.
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "모임 수정 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @OpenApiBody(
        content = [
            Content(
                encoding = [
                    Encoding(
                        name = "commandGatheringUpdateRequestVO",
                        contentType = MediaType.APPLICATION_JSON_VALUE,
                    ),
                ],
            ),
        ],
    )
    @PatchMapping("", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun patchGathering(
        @RequestHeader("idempotencyKey") idempotencyKey: String,
        @RequestPart("commandGatheringUpdateRequestVO") commandGatheringUpdateRequestVO: CommandGatheringUpdateRequestVO,
        @RequestPart("files", required = false) files: List<MultipartFile>?,
    ): ResponseDTO<*> {
        val commandGatheringUpdateDTO = gatheringConverter.updateRequestVOToDTO(commandGatheringUpdateRequestVO, idempotencyKey)

        commandGatheringService.updateGathering(commandGatheringUpdateDTO, files ?: emptyList())

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "모임 삭제",
        description =
            """
            모임 번호와 회원 번호를 이용하여 삭제합니다.
            회원 번호는 검증 용도로 사용됩니다.(추후 로직 상에서 구현 예정)
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "모임 삭제 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @DeleteMapping("")
    fun deleteGathering(
        @RequestHeader("idempotencyKey") idempotencyKey: String,
        @RequestBody commandGatheringDeleteRequestVO: CommandGatheringDeleteRequestVO,
    ): ResponseDTO<*> {
        val commandGatheringDeleteDTO = gatheringConverter.deleteRequestVOToDTO(commandGatheringDeleteRequestVO, idempotencyKey)

        commandGatheringService.deleteGathering(commandGatheringDeleteDTO)

        return ResponseDTO.ok(null)
    }

    // 모임 권한 수정 api
    @Operation(
        summary = "모임 권한 수정",
        description =
            """
            입력된 모임 정보, 멤버 id, 바뀐 권한정보를 바탕으로 모임 별 모임원 테이블을 수정합니다.
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "모임 권한 수정 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @PatchMapping("authorization")
    fun patchGatheringAuthorization(
        @RequestHeader("idempotencyKey") idempotencyKey: String,
        @RequestBody commandGatheringAuthorizationUpdateRequestVO: CommandGatheringAuthorizationUpdateRequestVO,
    ): ResponseDTO<*> {
        val commandGatheringAuthorizationUpdateDTO =
            gatheringConverter.updateAuthorizationRequestVOToDTO(
                commandGatheringAuthorizationUpdateRequestVO,
                idempotencyKey,
            )

        commandGatheringService.updateGatheringAuthorization(commandGatheringAuthorizationUpdateDTO)

        return ResponseDTO.ok(null)
    }

    // 모임 참여 api
    @Operation(
        summary = "모임 참여",
        description =
            """
            입력된 모임 정보, 멤버 id를 바탕으로 모임에 참여합니다.
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "모임 참여 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @PostMapping("member")
    fun postGatheringMember(
        @RequestHeader("idempotencyKey") idempotencyKey: String,
        @RequestParam("gatheringId") gatheringId: String,
        @Parameter(hidden = true) @CurrentMemberId memberId: String,
    ): ResponseDTO<*> {
        val commandGatheringMemberCreateDTO =
            gatheringConverter.memberRequestVOToDTO(
                gatheringId,
                memberId,
                idempotencyKey,
            )

        commandGatheringService.createGatheringMember(commandGatheringMemberCreateDTO)

        return ResponseDTO.ok(null)
    }

    // 모임 탈퇴 api(본인)
    @Operation(
        summary = "모임 탈퇴(본인)",
        description =
            """
            입력된 모임 정보, 멤버 id를 바탕으로 모임에 탈퇴합니다.(본인)
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "모임 탈퇴 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @DeleteMapping("member")
    fun deleteGatheringMember(
        @RequestHeader("idempotencyKey") idempotencyKey: String,
        @RequestParam("gatheringId") gatheringId: String,
        @Parameter(hidden = true) @CurrentMemberId memberId: String,
    ): ResponseDTO<*> {
        val commandGatheringMemberCreateDTO =
            gatheringConverter.memberRequestVOToDTO(
                gatheringId,
                memberId,
                idempotencyKey,
            )

        commandGatheringService.deleteGatheringMember(commandGatheringMemberCreateDTO)

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "모임 탈퇴(운영진)",
        description =
            """
            입력된 모임 정보, 멤버 id를 바탕으로 모임에 퇴장시킵니다.(운영진)
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "모임 탈퇴 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @DeleteMapping("executive")
    fun deleteGatheringMemberByExecutive(
        @RequestHeader("idempotencyKey") idempotencyKey: String,
        @RequestBody commandGatheringMemberDeleteRequestVO: CommandGatheringMemberDeleteRequestVO,
    ): ResponseDTO<*> {
        val commandGatheringMemberCreateDTO =
            gatheringConverter.deleteMemberRequestVOToDTO(
                commandGatheringMemberDeleteRequestVO,
                idempotencyKey,
            )

        commandGatheringService.deleteGatheringMember(commandGatheringMemberCreateDTO)

        return ResponseDTO.ok(null)
    }
}