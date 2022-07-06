package com.fu.flix.service.impl;

import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.service.CloudStorageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static com.fu.flix.constant.Constant.FILE_MUST_BE_IMAGE;

@RunWith(SpringRunner.class)
@SpringBootTest
class CloudStorageServiceImplTest {

    @Autowired
    CloudStorageService cloudStorageService;

    //    @Test
    void upload_image_jpg_success() throws IOException {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());

        // when
        String url = cloudStorageService.uploadImage(avatar);

        // then
        Assertions.assertTrue(url.length() > 0);
    }

    //    @Test
    void upload_image_jpeg_success() throws IOException {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "image.jpeg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());

        // when
        String url = cloudStorageService.uploadImage(avatar);

        // then
        Assertions.assertTrue(url.length() > 0);
    }

    //    @Test
    void upload_image_png_success() throws IOException {
        //given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "image.png",
                MediaType.IMAGE_PNG_VALUE,
                "avatar".getBytes());

        // when
        String url = cloudStorageService.uploadImage(avatar);

        // then
        Assertions.assertTrue(url.length() > 0);
    }

    //    @Test
    void upload_image_gif_success() throws IOException {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "image.gif",
                MediaType.IMAGE_GIF_VALUE,
                "avatar".getBytes());

        // when
        String url = cloudStorageService.uploadImage(avatar);

        // then
        Assertions.assertTrue(url.length() > 0);
    }

    @Test
    void upload_image_fail_when_image_is_null() {
        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> cloudStorageService.uploadImage(null));

        // then
        Assertions.assertEquals(FILE_MUST_BE_IMAGE, exception.getMessage());
    }

    @Test
    void upload_image_fail_when_file_extension_is_txt() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "image.txt",
                MediaType.TEXT_XML_VALUE,
                "avatar".getBytes());

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> cloudStorageService.uploadImage(avatar));

        // then
        Assertions.assertEquals(FILE_MUST_BE_IMAGE, exception.getMessage());
    }

    @Test
    void upload_image_fail_when_file_no_extension() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "image",
                MediaType.TEXT_XML_VALUE,
                "avatar".getBytes());

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> cloudStorageService.uploadImage(avatar));

        // then
        Assertions.assertEquals(FILE_MUST_BE_IMAGE, exception.getMessage());
    }
}