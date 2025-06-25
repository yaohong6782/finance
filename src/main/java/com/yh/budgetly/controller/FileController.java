package com.yh.budgetly.controller;

import com.yh.budgetly.config.FileStorageProperties;
import com.yh.budgetly.constants.Utils;
import com.yh.budgetly.exceptions.ErrorResponse;
import com.yh.budgetly.rest.dtos.FileDTO;
import com.yh.budgetly.rest.dtos.FileResourceDTO;
import com.yh.budgetly.rest.responses.dashboard.DashboardResponse;
import com.yh.budgetly.service.FileService;
import com.yh.budgetly.service.SupabaseStorageService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;


@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    private final FileStorageProperties uploadDir;
    private final SupabaseStorageService supabaseStorageService;

    @Tag(name = "Receipt files", description = "This API retrieves the byte data of Receipts")
    @GetMapping("/getReceipts/{fileId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receipt data retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Unable to find requested endpoint",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<byte[]> receiptFileData(@PathVariable Long fileId) {
        log.info("File id : {} ", fileId);

        FileDTO fileDTO = fileService.getFileDTO(fileId);
        byte[] fileData = fileService.fileReceiptData(fileId);

        String fileName = fileDTO.getFileName();
        String fileExtension = fileDTO.getFileType();

        MediaType mediaType = switch (Optional.ofNullable(fileExtension).orElse("")) {
            case "application/pdf" -> MediaType.APPLICATION_PDF;
            case "image/png" -> MediaType.IMAGE_PNG;
            case "image/jpeg" -> MediaType.IMAGE_JPEG;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);

        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }


    @GetMapping("/getFile/{fileName}")
    public Mono<ResponseEntity<String>> getReceiptFile(@PathVariable String fileName) {
        log.info("getting receipt file name : {} ", fileName);

        return supabaseStorageService.signedBucketFile(fileName, 3600)
                .map(signedUrl -> ResponseEntity.ok().body(signedUrl))
                .defaultIfEmpty(ResponseEntity.notFound().build());

    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        Path filePath = Paths.get(uploadDir.getUploadDir()).resolve(fileName).normalize();
        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        FileDTO fileDTO = fileService.getFileDTO(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileDTO.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}
