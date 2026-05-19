package com.diploma.Diplom.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A certificate for completing a course")
@Data
@Document(collection = "certificates")
@CompoundIndexes({
    @CompoundIndex(
        name = "unique_user_course_certificate",
        def = "{'userId': 1, 'courseId': 1}",
        unique = true
    )
})
public class Certificate {
    @Schema(description = "MongoDB ObjectId")
    @Id
    private String id;
    @Schema(description = "ID of the student who earned the certificate")
    private String userId;
    @Schema(description = "ID of the course for which the certificate was issued")
    private String courseId;

    @Schema(description = "Name of the student who earned the certificate")
    private String studentName;
    @Schema(description = "Title of the course for which the certificate was issued")
    private String courseTitle;
    @Schema(description = "Name of the instructor who taught the course")
    private String instructorName;

    @Schema(description = "Unique number for the certificate, used for verification")
    @Indexed(unique = true)
    private String certificateNumber;
    @Schema(description = "Verification code for the certificate, used for verification")
    @Indexed(unique = true)
    private String verificationCode;
    @Schema(description = "Timestamp when the certificate was issued", example = "2023-01-01T00:00:00")
    private LocalDateTime issuedAt;
    @Schema(description = "Timestamp when the certificate was regenerated", example = "2023-01-01T00:00:00")
    private LocalDateTime regeneratedAt;
    @Schema(description = "Version of the certificate template used to generate this certificate")
    private String templateVersion;
    @Schema(description = "URL to the PDF version of the certificate")
    private String pdfUrl;
    @Schema(description = "Indicates whether the certificate is active and valid for verification")
    private boolean active;
}