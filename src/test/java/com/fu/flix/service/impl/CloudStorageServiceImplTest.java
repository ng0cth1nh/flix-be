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

import javax.transaction.Transactional;

import java.io.IOException;

import static com.fu.flix.constant.Constant.FILE_MUST_BE_IMAGE;
import static com.fu.flix.constant.Constant.FILE_MUST_BE_IMAGE_OR_PDF;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class CloudStorageServiceImplTest {

    @Autowired
    CloudStorageService underTest;

    @Test
    void upload_image_fail_when_image_is_null() {
        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.uploadImage(null));

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
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.uploadImage(avatar));

        // then
        Assertions.assertEquals(FILE_MUST_BE_IMAGE, exception.getMessage());
    }

    @Test
    void test_is_Not_CertificateFile() {
        // when
        boolean check = underTest.isCertificateFile(null);

        // then
        Assertions.assertFalse(check);
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
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.uploadImage(avatar));

        // then
        Assertions.assertEquals(FILE_MUST_BE_IMAGE, exception.getMessage());
    }

    @Test
    void test_uploadCertificateFile_fail_when_file_is_txt() {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "image.txt",
                MediaType.TEXT_XML_VALUE,
                "avatar".getBytes());

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.uploadCertificateFile(avatar));

        // then
        Assertions.assertEquals(FILE_MUST_BE_IMAGE_OR_PDF, exception.getMessage());
    }

}