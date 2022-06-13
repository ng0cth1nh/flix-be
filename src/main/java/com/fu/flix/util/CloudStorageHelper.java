package com.fu.flix.util;

import com.fu.flix.dto.error.GeneralException;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static com.fu.flix.constant.Constant.FILE_MUST_BE_IMAGE;

@Slf4j
public class CloudStorageHelper {

    private static Credentials credentials;
    private static final Storage STORAGE = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

    public CloudStorageHelper() {
        try {
            File file = ResourceUtils.getFile("classpath:cloud_authen.json");
            credentials = GoogleCredentials.fromStream(new FileInputStream(file));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public static String uploadImage(MultipartFile fileStream) throws IOException {
        String bucketName = "flix_public";

        if (!isImageFile(fileStream.getOriginalFilename())) {
            throw new GeneralException(FILE_MUST_BE_IMAGE);
        }

        final String fileName = UUID.randomUUID() + fileStream.getOriginalFilename();

        BlobInfo blobInfo = STORAGE.create(
                BlobInfo.newBuilder(bucketName, fileName)
                        // Modify access list to allow all users with link to read file
                        .setAcl(new ArrayList<>(Arrays.asList(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER))))
                        .build(),
                fileStream.getInputStream());

        log.info("Image URL: " + blobInfo.getMediaLink());
        return blobInfo.getMediaLink();
    }

    private static boolean isImageFile(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            String[] allowedExt = {".jpg", ".jpeg", ".png", ".gif"};
            for (String ext : allowedExt) {
                if (fileName.endsWith(ext)) {
                    return true;
                }
            }
        }
        return false;
    }
}
