package com.bockerl.snailmember.blob.command.application.service

import com.bockerl.snailmember.blob.command.application.dto.CommandFileDTO
import com.bockerl.snailmember.blob.command.domain.aggregate.vo.CommandFileRequestVO
import org.springframework.web.multipart.MultipartFile

interface CommandFileService{

    fun uploadFiles(file: List<MultipartFile>, commandFileDTO: CommandFileDTO)

    fun downloadFile(fileName: String): ByteArray

    fun deleteFile(commandFileRequestVO: CommandFileRequestVO)
}