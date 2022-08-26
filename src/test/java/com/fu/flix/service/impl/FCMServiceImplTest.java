package com.fu.flix.service.impl;

import com.fu.flix.constant.enums.RoleType;
import com.fu.flix.dao.UserDAO;
import com.fu.flix.dto.request.SaveFCMTokenRequest;
import com.fu.flix.dto.response.SaveFCMTokenResponse;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.entity.User;
import com.fu.flix.service.FCMService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.fu.flix.constant.Constant.SAVE_FCM_TOKEN_SUCCESS;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Transactional
class FCMServiceImplTest {
    @InjectMocks
    FCMServiceImpl underTest;
    @Mock
    User user;
    @Mock
    UserDAO userDAO;

    @BeforeEach
    void setup() {
        user = new User();
    }

    @Test
    void test_saveFCMToken_success() {

        // given
        SaveFCMTokenRequest request = new SaveFCMTokenRequest();
        request.setToken("dPiyIPmpSvObXK08aNVkoH:APA91bEehh5lwgocYkHEnoNG6ssTisJpVSGZpmnfPkEuNbvG8yul8HHX25e1id2Sx9Aaz9GIn-sq1QOUzRM4MPbe2r8JvlkvL_ZfwIYSpk1Qq7KA0vPBocEKIN0ZYOtbqywNDngMp-is");

        // when
        Mockito.when(userDAO.findById(36L)).thenReturn(Optional.of(user));
        setCustomerContext(36L, "0865390037");
        SaveFCMTokenResponse response = underTest.saveFCMToken(request).getBody();

        // then
        Assertions.assertEquals(SAVE_FCM_TOKEN_SUCCESS, response.getMessage());
    }

    void setCustomerContext(Long id, String phone) {
        List<String> roles = new ArrayList<>();
        roles.add(RoleType.ROLE_CUSTOMER.name());
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(new UserPrincipal(id, phone, roles), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}