package com.diploma.Diplom.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadRootDir;

    public FileUploadResult saveFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            Path targetDir = Paths.get(uploadRootDir, folder);
            Files.createDirectories(targetDir);

            String originalFileName = file.getOriginalFilename();
            String safeOriginalName = originalFileName == null
                    ? "file"
                    : originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");

            String storedFileName = UUID.randomUUID() + "_" + safeOriginalName;

            Path targetPath = targetDir.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            FileUploadResult result = new FileUploadResult();
            result.setFileName(safeOriginalName);
            result.setFileUrl(Paths.get(folder, storedFileName).toString().replace("\\", "/"));

            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileUrl) {
    if (fileUrl == null || fileUrl.isBlank()) {
        return;
    }

    try {
        Path path = Paths.get(uploadRootDir, fileUrl).normalize();
        Files.deleteIfExists(path);
    } catch (IOException e) {
        throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
    }
    }

    public static class FileUploadResult {
        private String fileName;
        private String fileUrl;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }
    }
}