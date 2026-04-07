package com.diploma.Diplom.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.Data;

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
            result.setFileUrl((String) uploadResult.get("secure_url"));  
            result.setPublicId((String) uploadResult.get("public_id"));  
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
        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "video"));
        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
    } catch (IOException e) {
        throw new RuntimeException("Cloudinary delete failed: " + e.getMessage(), e);
    }
}


    @Data
    public static class FileUploadResult {
        private String fileUrl;
        private String publicId;
        private String fileName;
    }
}