package com.diploma.Diplom.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public FileUploadResult uploadFile(MultipartFile file, String folder) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "auto"
                )
            );

            FileUploadResult result = new FileUploadResult();
            result.setFileUrl((String) uploadResult.get("secure_url"));  // https://res.cloudinary.com/...
            result.setPublicId((String) uploadResult.get("public_id"));  // для удаления
            result.setFileName(file.getOriginalFilename());

            return result;

        } catch (IOException e) {
            throw new RuntimeException("Cloudinary upload failed: " + e.getMessage(), e);
        }
    }

    // принимает publicId а не url
    public void deleteFile(String publicId) {
        if (publicId == null || publicId.isBlank()) return;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                "resource_type", "auto" // важно для видео и pdf
            ));
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary delete failed: " + e.getMessage(), e);
        }
    }

    public static class FileUploadResult {
        private String fileUrl;
        private String publicId;
        private String fileName;

        public String getFileUrl() { return fileUrl; }
        public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

        public String getPublicId() { return publicId; }
        public void setPublicId(String publicId) { this.publicId = publicId; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
    }
}