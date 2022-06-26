package com.fu.flix.service.impl;

import com.fu.flix.dao.UserAddressDAO;
import com.fu.flix.dao.UserDAO;
import com.fu.flix.dto.NotificationDTO;
import com.fu.flix.dto.request.NotificationRequest;
import com.fu.flix.dto.response.NotificationResponse;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
class UserServiceImplTest {
    @Autowired
    UserAddressDAO userAddressDAO;
    @Autowired
    UserDAO userDAO;

    @Autowired
    UserService userService;

    @Test
    void should_get_notifications() {
        // given
        Long id = 36L;
        String phone = "0865390037";
        NotificationRequest request = new NotificationRequest();
        setContextUsername(id, phone);

        // when
        ResponseEntity<NotificationResponse> responseEntity = userService.getNotifications(request);
        NotificationResponse response = responseEntity.getBody();
        List<NotificationDTO> notificationDTOS = response.getNotifications();

        // then
        Assertions.assertEquals("Thông báo demo", notificationDTOS.get(0).getTitle());
        Assertions.assertEquals(3, notificationDTOS.size());
    }

    void setContextUsername(Long id, String phone) {
        String[] roles = {"ROLE_CUSTOMER"};
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(new UserPrincipal(id, phone), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}