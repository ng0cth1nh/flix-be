package com.fu.flix.service.impl;

import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.service.CloudStorageService;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static com.fu.flix.constant.Constant.FILE_MUST_BE_IMAGE;
import static com.fu.flix.constant.Constant.FILE_MUST_BE_IMAGE_OR_PDF;

@Slf4j
@Service
public class CloudStorageServiceImpl implements CloudStorageService {
    private final String BUCKET_NAME = "flix_public";
    private final String CLOUD_AUTHENTICATION_FILE_NAME = "/cloud_authen.json";
    private final Credentials CREDENTIALS;
    private final Storage STORAGE;

    public CloudStorageServiceImpl() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(CLOUD_AUTHENTICATION_FILE_NAME);
        CREDENTIALS = GoogleCredentials.fromStream(inputStream);
        STORAGE = StorageOptions.newBuilder().setCredentials(CREDENTIALS).build().getService();
    }

    @Override
    public String uploadImage(MultipartFile fileStream) throws IOException {
        if (!isImageFile(fileStream)) {
            throw new GeneralException(HttpStatus.GONE, FILE_MUST_BE_IMAGE);
        }

        final String fileName = UUID.randomUUID() + fileStream.getOriginalFilename();
        BlobInfo blobInfo = uploadFile(fileStream, fileName);

        log.info("Image URL: " + blobInfo.getMediaLink());
        return blobInfo.getMediaLink();
    }

    @Override
    public String uploadCertificateFile(MultipartFile fileStream) throws IOException {
        if (!isCertificateFile(fileStream)) {
            throw new GeneralException(HttpStatus.GONE, FILE_MUST_BE_IMAGE_OR_PDF);
        }

        final String fileName = UUID.randomUUID() + fileStream.getOriginalFilename();
        BlobInfo blobInfo = uploadFile(fileStream, fileName);

        log.info("File URL: " + blobInfo.getMediaLink());
        return blobInfo.getMediaLink();
    }

    private BlobInfo uploadFile(MultipartFile fileStream, String fileName) throws IOException {
        return STORAGE.create(
                BlobInfo.newBuilder(BUCKET_NAME, fileName)
                        // Modify access list to allow all users with link to read file
                        .setAcl(new ArrayList<>(Collections.singletonList(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER))))
                        .build(),
                fileStream.getInputStream());
    }

    @Override
    public boolean isCertificateFile(MultipartFile fileStream) {
        if (fileStream != null) {
            String fileName = fileStream.getOriginalFilename();
            if (fileName != null && fileName.contains(".")) {
                return isValidCertificateFileExtension(fileName);
            }
        }
        return false;
    }

    private boolean isValidCertificateFileExtension(String fileName) {
        String[] allowedExt = {".jpg", ".jpeg", ".png", ".gif", ".pdf"};
        for (String ext : allowedExt) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isImageFile(MultipartFile fileStream) {
        if (fileStream != null) {
            String fileName = fileStream.getOriginalFilename();
            if (fileName != null && fileName.contains(".")) {
                return isValidImageExtension(fileName);
            }
        }
        return false;
    }

    private boolean isValidImageExtension(String fileName) {
        String[] allowedExt = {".jpg", ".jpeg", ".png", ".gif"};
        for (String ext : allowedExt) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
}
