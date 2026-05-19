package com.diploma.Diplom.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.diploma.Diplom.exception.BadRequestException;
import com.diploma.Diplom.exception.InternalServerException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files")
@Tag(name = "Files", description = "Serve uploaded files such as PDFs, images, and videos")
public class FileController {

    @Value("${app.upload.dir:uploads}")
    private String uploadRootDir;

    @Operation(
        summary = "Get uploaded file by relative path",
        description = """
            Returns a file from the upload directory by its relative path.

            Supported inline preview types:
            - PDF
            - PNG
            - JPG / JPEG
            - GIF
            - MP4
            - WEBM

            Other file types are returned as downloadable attachments.

            Example:
            `/files?path=certificates/certificate-123.pdf`
            """,
        parameters = {
            @Parameter(
                name = "path",
                description = "Relative path to the file inside the upload directory",
                required = true,
                in = ParameterIn.QUERY,
                example = "certificates/certificate-123.pdf"
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "File returned successfully",
                content = @Content(
                    mediaType = "*/*",
                    schema = @Schema(type = "string", format = "binary")
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid file path",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "404",
                description = "File not found",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Failed to read file",
                content = @Content
            )
        }
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> getFile(@RequestParam("path") String filePath) {
        try {
            Path rootPath = Paths.get(uploadRootDir).toAbsolutePath().normalize();
            Path resolvedPath = rootPath.resolve(filePath.replace("\\", "/")).normalize();

            if (!resolvedPath.startsWith(rootPath)) {
                throw new BadRequestException("Invalid file path");
            }

            String normalizedName = resolvedPath.getFileName().toString().toLowerCase();
            if (!isAllowedExtension(normalizedName)) {
                throw new BadRequestException("File type not allowed");
            }

            Resource resource = new UrlResource(resolvedPath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new BadRequestException("File not found");
            }

            String contentType = detectContentType(resolvedPath);
            boolean inline = isInlineType(contentType);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDisposition(
                    ContentDisposition.builder(inline ? "inline" : "attachment")
                            .filename(resolvedPath.getFileName().toString())
                            .build()
            );

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new InternalServerException("Failed to read file: " + e.getMessage());
        }
    }

    private boolean isAllowedExtension(String fileName) {
        return fileName.endsWith(".pdf") || fileName.endsWith(".png") ||
               fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
               fileName.endsWith(".gif") || fileName.endsWith(".mp4") ||
               fileName.endsWith(".webm");
    }

    private String detectContentType(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF_VALUE;
        }
        if (fileName.endsWith(".png")) {
            return MediaType.IMAGE_PNG_VALUE;
        }
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG_VALUE;
        }
        if (fileName.endsWith(".gif")) {
            return MediaType.IMAGE_GIF_VALUE;
        }
        if (fileName.endsWith(".mp4")) {
            return "video/mp4";
        }
        if (fileName.endsWith(".webm")) {
            return "video/webm";
        }

        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    private boolean isInlineType(String contentType) {
        return contentType.startsWith("image/")
                || contentType.equals(MediaType.APPLICATION_PDF_VALUE)
                || contentType.startsWith("video/");
    }
}