package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.dao.UserDAO;
import com.fu.flix.dto.request.NotificationRequest;
import com.fu.flix.dto.request.UpdateAvatarRequest;
import com.fu.flix.dto.response.NotificationResponse;
import com.fu.flix.dto.response.UpdateAvatarResponse;
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

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static com.fu.flix.constant.Constant.UPDATE_AVATAR_SUCCESS;

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