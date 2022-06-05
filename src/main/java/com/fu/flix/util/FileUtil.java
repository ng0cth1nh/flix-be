package com.fu.flix.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public class FileUtil {
    public static String uploadImage(MultipartFile image) {
        return UUID.randomUUID() + ".jpg";
    }
}
