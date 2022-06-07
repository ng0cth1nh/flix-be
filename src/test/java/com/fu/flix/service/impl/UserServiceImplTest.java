package com.fu.flix.service.impl;

import com.fu.flix.dao.UserAddressDAO;
import com.fu.flix.dao.UserDAO;
import com.fu.flix.dto.request.MainAddressRequest;
import com.fu.flix.dto.request.UserAddressRequest;
import com.fu.flix.dto.response.MainAddressResponse;
import com.fu.flix.dto.response.UserAddressResponse;
import com.fu.flix.entity.User;
import com.fu.flix.entity.UserAddress;
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
import java.util.Optional;


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
    void test_get_user_address() {
        // given
        String phone = "0865390037";
        Optional<User> optionalUser = userDAO.findByUsername(phone);

        // when
        User user = optionalUser.get();
        Optional<UserAddress> optionalUserAddress = userAddressDAO.findByUserAndIsMainAddress(user, true);
        UserAddress userAddress = optionalUserAddress.get();

        // then
        Assertions.assertEquals(phone, userAddress.getPhone());
        Assertions.assertEquals("00001", userAddress.getCommune().getId());
    }

    @Test
    void test_get_main_address() {

        // given
        String phone = "0865390037";
        MainAddressRequest request = new MainAddressRequest();
        setContextUsername(phone);

        // when
        ResponseEntity<MainAddressResponse> responseEntity = userService.getMainAddress(request);
        MainAddressResponse mainAddressResponse = responseEntity.getBody();

        // then
        Assertions.assertEquals(phone, mainAddressResponse.getPhone());
        Assertions.assertEquals("Sơn Tùng MTP", mainAddressResponse.getCustomerName());
        Assertions.assertEquals("68 Hoàng Hoa Thám, Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội", mainAddressResponse.getAddressName());
    }

    @Test
    void test_get_user_addresses() {
        // given
        String phone = "0865390039";
        UserAddressRequest request = new UserAddressRequest();
        setContextUsername(phone);

        // when
        ResponseEntity<UserAddressResponse> responseEntity = userService.getUserAddresses(request);
        UserAddressResponse userAddressResponse = responseEntity.getBody();

        // then
        Assertions.assertEquals(1, userAddressResponse.getAddresses().size());

    }

    void setContextUsername(String phone) {
        String[] roles = {"ROLE_CUSTOMER"};
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(phone, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}