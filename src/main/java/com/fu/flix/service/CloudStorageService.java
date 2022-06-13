package com.fu.flix.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudStorageService {
    String uploadImage(MultipartFile fileStream) throws IOException;
}
