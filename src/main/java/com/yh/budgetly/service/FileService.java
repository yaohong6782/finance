package com.yh.budgetly.service;

import com.yh.budgetly.config.FileStorageProperties;
import com.yh.budgetly.entity.FileEntity;
import com.yh.budgetly.repository.FileRepository;
import com.yh.budgetly.rest.dtos.FileDTO;
import com.yh.budgetly.rest.dtos.FileResourceDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Builder
@Service
@AllArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final FileStorageProperties uploadDir;

    public byte[] fileReceiptData(Long fileId) {

        FileEntity file = fileRepository.getFileByFileId(fileId).orElse(null);

        if (file != null) {
            return file.getFileData();
        }
        return null;
    }

    public FileDTO getFileDTO(Long fileId) {
        FileEntity file = fileRepository.getFileByFileId(fileId).orElse(null);

        if (file == null) return new FileDTO();

        return FileDTO.builder()
                .fileName(file.getFileName())
                .fileUrl("http://localhost:8085/api/files/getReceipts/" + file.getFileId())
                .fileType(file.getFileType())
                .uploadedAt(file.getUploadedAt())
                .build();
    }

    public FileDTO getFileDTO(String fileName) {
        FileEntity file = fileRepository.getFileByBucketFileName(fileName)
                .orElse(null);

        if (file == null) return new FileDTO();

        return FileDTO.builder()
                .fileName(file.getFileName())
                .fileType(file.getFileType())
                .uploadedAt(file.getUploadedAt())
                .build();
    }

    public FileResourceDTO getReceipt(String fileName) {

        try {
            log.info("Getting receipt");
//            Path filePath = Paths.get(uploadDir.getUploadDir()).resolve(fileName).normalize();
//            Resource resource = new UrlResource(filePath.toUri());
            String publicUrl = "https://srismgquzuxjkdqpeixy.supabase.co/storage/v1/object/public/finance-receipts/" + fileName;
            Resource resource = new UrlResource(publicUrl);

//            if (!resource.exists() || !resource.isReadable()) {
//                throw new RuntimeException("File not found or not readable: " + fileName);
//            }

            FileDTO fileDTO = getFileDTO(fileName);
            String fileMediaType = fileDTO.getFileType();

            MediaType mediaType;
            try {
                mediaType = MediaType.parseMediaType(fileMediaType);
            } catch (IllegalArgumentException e) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }

            return new FileResourceDTO(resource, mediaType);

        } catch (MalformedURLException e) {
            log.error("Error : {} ", e.getMessage());
            throw new RuntimeException("Invalid file URL: " + fileName, e);
        }
    }
}
