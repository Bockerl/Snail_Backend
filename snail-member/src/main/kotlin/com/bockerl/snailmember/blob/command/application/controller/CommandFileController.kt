package com.bockerl.snailmember.blob.command.application.controller

import com.bockerl.snailmember.blob.command.application.dto.CommandFileDTO
import com.bockerl.snailmember.blob.command.application.service.CommandFileService
import com.bockerl.snailmember.blob.command.domain.aggregate.vo.CommandFileRequestVO
import com.bockerl.snailmember.common.ResponseDTO
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/file")
class CommandFileController(
    private val commandFileService: CommandFileService
) {

    @PostMapping("")
    fun postFiles(
        @RequestPart("files") files: List<MultipartFile>,
        @RequestPart("commandFileRequestVO") commandFileRequestVO: CommandFileRequestVO,
    ): ResponseDTO<Void> {

        commandFileService.uploadFiles(files, commandFileRequestVO)

        return ResponseDTO.ok(null)
    }

    @PatchMapping("")
    fun patchFiles(
        @RequestPart("commandFileRequestVO") commandFileRequestVO: CommandFileRequestVO,
        @RequestPart("deleteFilesIds") deleteFilesIds: List<Long>,
        @RequestPart("newFiles") newFiles: List<MultipartFile>,
    ) :ResponseDTO<Void> {

        commandFileService.updateFiles(commandFileRequestVO, deleteFilesIds, newFiles)

        return ResponseDTO.ok(null);
    }

    @DeleteMapping("")
    fun deleteFile(@RequestBody commandFileRequestVO: CommandFileRequestVO): ResponseDTO<Void> {
        commandFileService.deleteFile(commandFileRequestVO)
        return ResponseDTO.ok(null);
    }


    @GetMapping("/download/{fileName}")
    fun downloadFile(@PathVariable fileName: String): ResponseEntity<ByteArray> {
        val data = commandFileService.downloadFile(fileName)

        /* 설명. 이거 responseDTO로 받을 수 있는 방법 알아보자*/
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .body(data)
    }
}