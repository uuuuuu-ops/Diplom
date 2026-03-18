package com.diploma.Diplom.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files")
public class FileController {

    @Value("${app.upload.dir:uploads}")
    private String uploadRootDir;

    @GetMapping
    public ResponseEntity<Resource> getFile(@RequestParam("path") String filePath) {
        try {
            Path rootPath = Paths.get(uploadRootDir).toAbsolutePath().normalize();
            Path resolvedPath = rootPath.resolve(filePath.replace("\\", "/")).normalize();

            if (!resolvedPath.startsWith(rootPath)) {
                throw new RuntimeException("Invalid file path");
            }

            Resource resource = new UrlResource(resolvedPath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("File not found");
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
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }
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