package com.bockerl.snailmember.blob.command.domain.service

import com.azure.storage.blob.BlobContainerClient
import com.bockerl.snailmember.blob.command.application.dto.CommandFileDTO
import com.bockerl.snailmember.blob.command.application.service.CommandFileService
import com.bockerl.snailmember.blob.command.domain.aggregate.entity.File
import com.bockerl.snailmember.blob.command.domain.aggregate.vo.CommandFileRequestVO
import com.bockerl.snailmember.blob.command.domain.repository.CommandFileRepository
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.util.*

@Service
class CommandFileServiceImpl(
    private val blobContainerClient: BlobContainerClient,
    private val commandFileRepository: CommandFileRepository
) : CommandFileService {

    @Transactional
    override fun uploadFiles(files: List<MultipartFile>, commandFileDTO: CommandFileDTO) {
        val fileEntities = mutableListOf<File>()

        /* 설명. 업로드 파일 수 제한 10개 초과 되지 않도록... */
        if(files.size > 10){
            throw CommonException(ErrorCode.TOO_MANY_FILES)
        }

        files.forEach { file ->
            val fileName = generateUniqueFileName(file.originalFilename)
            val blobClient = blobContainerClient.getBlobClient(fileName)
            blobClient.upload(file.inputStream, file.size, true)
            val fileUrl = blobClient.blobUrl

            val fileEntity = File(
                fileName = fileName,
                fileType = file.contentType ?: "unknown",
                fileUrl = fileUrl,
                fileTargetType = commandFileDTO.fileTargetType,
                fileTargetId = commandFileDTO.fileTargetId,
                memberId = commandFileDTO.memberId // 필요한 경우 추가
            )
            fileEntities.add(fileEntity)
        }

        commandFileRepository.saveAll(fileEntities)
    }

    /* 설명.
         *  Filename을 받아 Blob에서 파일을 다운로드함.
         *  ByteArrayOutputStream을 사용해서 바이너리 데이터를 읽어옴
        * */
    override fun downloadFile(fileName: String): ByteArray {
        val blobClient = blobContainerClient.getBlobClient(fileName)
        val outputStream = ByteArrayOutputStream()
        blobClient.download(outputStream)
        return outputStream.toByteArray()
    }

    /* 설명.
     *  fileName에 해당하는 Blob을 삭제함.
    * */
    override fun deleteFile(commandFileRequestVO: CommandFileRequestVO) {

        val files = commandFileRepository.findByFileTargetTypeAndFileTargetId(commandFileRequestVO.fileTargetType, commandFileRequestVO.fileTargetId)

        for(file in files){
            val blobClient = blobContainerClient.getBlobClient(file.fileName)
            blobClient.delete()
        }

        commandFileRepository.deleteAll(files)
    }

    /* 설명. uuid 생성으로 파일 이름 중복 방지 */
    private fun generateUniqueFileName(originalFileName: String?): String{
        val uuid = UUID.randomUUID().toString()
        /* 설명. 확장자 파싱 */
        val extension = originalFileName?.substringAfterLast(".", "")?: ""
        return if(extension.isNotEmpty()) "$uuid-$extension" else uuid
    }
}