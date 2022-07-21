package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.dao.UserDAO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.ChangePasswordRequest;
import com.fu.flix.dto.request.NotificationRequest;
import com.fu.flix.dto.request.UpdateAvatarRequest;
import com.fu.flix.dto.request.UserCreateFeedbackRequest;
import com.fu.flix.dto.response.ChangePasswordResponse;
import com.fu.flix.dto.response.NotificationResponse;
import com.fu.flix.dto.response.UpdateAvatarResponse;
import com.fu.flix.dto.response.UserCreateFeedbackResponse;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.entity.User;
import com.fu.flix.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.fu.flix.constant.Constant.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class UserServiceImplTest {
    @Autowired
    UserService underTest;

    @Autowired
    UserDAO userDAO;

    @Autowired
    AppConf appConf;

    @Test
    public void test_update_avatar_success() throws IOException {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());

        UpdateAvatarRequest request = new UpdateAvatarRequest();
        request.setAvatar(avatar);
        setCustomerContext(36L, "0865390037");

        // when
        UpdateAvatarResponse response = underTest.updateAvatar(request).getBody();

        // then
        Assertions.assertEquals(UPDATE_AVATAR_SUCCESS, response.getMessage());
    }

    @Test
    public void test_update_avatar_success_when_old_avatar_is_default() throws IOException {
        // given
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());

        UpdateAvatarRequest request = new UpdateAvatarRequest();
        request.setAvatar(avatar);

        long userId = 36L;
        User user = userDAO.findById(userId).get();
        user.setAvatar(appConf.getDefaultAvatar());

        setCustomerContext(userId, "0865390037");

        // when
        UpdateAvatarResponse response = underTest.updateAvatar(request).getBody();

        // then
        Assertions.assertEquals(UPDATE_AVATAR_SUCCESS, response.getMessage());
    }

    @Test
    public void test_get_notifications_success() {
        // given
        NotificationRequest request = new NotificationRequest();
        setCustomerContext(36L, "0865390037");

        // when
        NotificationResponse response = underTest.getNotifications(request).getBody();

        // then
        Assertions.assertNotNull(response.getNotifications());
    }

    @Test
    public void test_change_password_success() {
        // given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("123abc");
        request.setNewPassword("abc123");

        // when
        setCustomerContext(36L, "0865390037");
        ChangePasswordResponse response = underTest.changePassword(request).getBody();

        // then
        Assertions.assertEquals(CHANGE_PASSWORD_SUCCESS, response.getMessage());
    }

    @Test
    public void test_change_password_fail_when_wrong_password() {
        // given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("123abcde");
        request.setNewPassword("abc123");

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.changePassword(request));

        // then
        Assertions.assertEquals(WRONG_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_change_password_fail_when_password_is_invalid() {
        // given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("123abc");
        request.setNewPassword("123");

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.changePassword(request));

        // then
        Assertions.assertEquals(INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_change_password_fail_when_old_pass_same_new_pass() {
        // given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("123abc");
        request.setNewPassword("123abc");

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.changePassword(request));

        // then
        Assertions.assertEquals(NEW_PASSWORD_MUST_BE_DIFFERENT_FROM_OLD_PASSWORD, exception.getMessage());
    }

    @Test
    public void test_create_feed_back_success_when_request_code_is_null() throws IOException {
        // given
        UserCreateFeedbackRequest request = new UserCreateFeedbackRequest();
        request.setRequestCode(null);
        request.setFeedbackType("COMMENT");
        request.setTitle("Hủy đơn của tao");
        request.setDescription("Thợ làm ăn chán vãi");
        request.setImages(getListImages());

        // when
        setCustomerContext(36L, "0865390037");
        UserCreateFeedbackResponse response = underTest.createFeedback(request).getBody();

        // then
        Assertions.assertEquals(CREATE_FEEDBACK_SUCCESS, response.getMessage());
    }

    @Test
    public void test_create_feed_back_success_when_request_code_is_not_null() throws IOException {
        // given
        UserCreateFeedbackRequest request = new UserCreateFeedbackRequest();
        request.setRequestCode("1107226GDG5F");
        request.setFeedbackType("COMMENT");
        request.setTitle("Hủy đơn của tao");
        request.setDescription("Thợ làm ăn chán vãi");
        request.setImages(getListImages());

        // when
        setCustomerContext(36L, "0865390037");
        UserCreateFeedbackResponse response = underTest.createFeedback(request).getBody();

        // then
        Assertions.assertEquals(CREATE_FEEDBACK_SUCCESS, response.getMessage());
    }

    @Test
    public void test_create_feed_back_fail_when_request_code_is_invalid() {
        // given
        UserCreateFeedbackRequest request = new UserCreateFeedbackRequest();
        request.setRequestCode("INVALID");
        request.setFeedbackType("COMMENT");
        request.setTitle("Hủy đơn của tao");
        request.setDescription("Thợ làm ăn chán vãi");
        request.setImages(getListImages());

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFeedback(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_create_feed_back_fail_when_request_code_is_empty() {
        // given
        UserCreateFeedbackRequest request = new UserCreateFeedbackRequest();
        request.setRequestCode("");
        request.setFeedbackType("COMMENT");
        request.setTitle("Hủy đơn của tao");
        request.setDescription("Thợ làm ăn chán vãi");
        request.setImages(getListImages());

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFeedback(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_create_feed_back_fail_when_title_is_empty() {
        // given
        UserCreateFeedbackRequest request = new UserCreateFeedbackRequest();
        request.setRequestCode("1107226GDG5F");
        request.setFeedbackType("COMMENT");
        request.setTitle("");
        request.setDescription("Thợ làm ăn chán vãi");
        request.setImages(getListImages());

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFeedback(request));

        // then
        Assertions.assertEquals(INVALID_TITLE, exception.getMessage());
    }

    @Test
    public void test_create_feed_back_fail_when_title_is_null() {
        // given
        UserCreateFeedbackRequest request = new UserCreateFeedbackRequest();
        request.setRequestCode("1107226GDG5F");
        request.setFeedbackType("COMMENT");
        request.setTitle(null);
        request.setDescription("Thợ làm ăn chán vãi");
        request.setImages(getListImages());

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFeedback(request));

        // then
        Assertions.assertEquals(INVALID_TITLE, exception.getMessage());
    }

    @Test
    public void test_create_feed_back_fail_when_description_is_empty() {
        // given
        UserCreateFeedbackRequest request = new UserCreateFeedbackRequest();
        request.setRequestCode("1107226GDG5F");
        request.setFeedbackType("COMMENT");
        request.setTitle("La la la");
        request.setDescription("");
        request.setImages(getListImages());

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFeedback(request));

        // then
        Assertions.assertEquals(INVALID_DESCRIPTION, exception.getMessage());
    }

    @Test
    public void test_create_feed_back_fail_when_description_is_null() {
        // given
        UserCreateFeedbackRequest request = new UserCreateFeedbackRequest();
        request.setRequestCode("1107226GDG5F");
        request.setFeedbackType("COMMENT");
        request.setTitle("La la la");
        request.setDescription(null);
        request.setImages(getListImages());

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFeedback(request));

        // then
        Assertions.assertEquals(INVALID_DESCRIPTION, exception.getMessage());
    }

    @Test
    public void test_create_feed_back_success_when_images_is_null() throws IOException {
        // given
        UserCreateFeedbackRequest request = new UserCreateFeedbackRequest();
        request.setRequestCode("1107226GDG5F");
        request.setFeedbackType("COMMENT");
        request.setTitle("Hủy đơn của tao");
        request.setDescription("Thợ làm ăn chán vãi");
        request.setImages(null);

        // when
        setCustomerContext(36L, "0865390037");
        UserCreateFeedbackResponse response = underTest.createFeedback(request).getBody();

        // then
        Assertions.assertEquals(CREATE_FEEDBACK_SUCCESS, response.getMessage());
    }

    @Test
    public void test_create_feed_back_fail_when_invalid_images() {
        // given
        UserCreateFeedbackRequest request = new UserCreateFeedbackRequest();
        request.setRequestCode("1107226GDG5F");
        request.setFeedbackType("COMMENT");
        request.setTitle("La la la");
        request.setDescription("la la la");
        request.setImages(getListImagesWrong());

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFeedback(request));

        // then
        Assertions.assertEquals(FILE_MUST_BE_IMAGE, exception.getMessage());
    }

    @Test
    public void test_create_feed_back_fail_when_feedback_type_is_empty() {
        // given
        UserCreateFeedbackRequest request = new UserCreateFeedbackRequest();
        request.setRequestCode("1107226GDG5F");
        request.setFeedbackType("");
        request.setTitle("La la la");
        request.setDescription("la la la");
        request.setImages(getListImages());

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFeedback(request));

        // then
        Assertions.assertEquals(INVALID_FEEDBACK_TYPE, exception.getMessage());
    }

    @Test
    public void test_create_feed_back_fail_when_feedback_type_is_null() {
        // given
        UserCreateFeedbackRequest request = new UserCreateFeedbackRequest();
        request.setRequestCode("1107226GDG5F");
        request.setFeedbackType(null);
        request.setTitle("La la la");
        request.setDescription("la la la");
        request.setImages(getListImages());

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFeedback(request));

        // then
        Assertions.assertEquals(INVALID_FEEDBACK_TYPE, exception.getMessage());
    }

    @Test
    public void test_create_feed_back_fail_when_feedback_type_is_invalid() {
        // given
        UserCreateFeedbackRequest request = new UserCreateFeedbackRequest();
        request.setRequestCode("1107226GDG5F");
        request.setFeedbackType("INVALID");
        request.setTitle("La la la");
        request.setDescription("la la la");
        request.setImages(getListImages());

        // when
        setCustomerContext(36L, "0865390037");
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> underTest.createFeedback(request));

        // then
        Assertions.assertEquals(INVALID_FEEDBACK_TYPE, exception.getMessage());
    }

    private List<MultipartFile> getListImages() {
        List<MultipartFile> images = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            MockMultipartFile image = new MockMultipartFile(
                    "avatar",
                    "filename.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "avatar".getBytes());
            images.add(image);
        }
        return images;
    }

    private List<MultipartFile> getListImagesWrong() {
        List<MultipartFile> images = new ArrayList<>();

        MockMultipartFile image1 = new MockMultipartFile(
                "avatar",
                "filename.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());

        MockMultipartFile image2 = new MockMultipartFile(
                "avatar",
                "filename.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "avatar".getBytes());

        images.add(image1);
        images.add(image2);

        return images;
    }

    void setRepairerContext(Long id, String phone) {
        String[] roles = {"ROLE_REPAIRER"};
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(new UserPrincipal(id, phone, roles), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    void setCustomerContext(Long id, String phone) {
        String[] roles = {"ROLE_CUSTOMER"};
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(new UserPrincipal(id, phone, roles), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}