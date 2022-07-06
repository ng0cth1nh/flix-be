package com.fu.flix.service.impl;

import com.fu.flix.constant.enums.CommentType;
import com.fu.flix.dao.CommentDAO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.CommentRequest;
import com.fu.flix.dto.response.CommentResponse;
import com.fu.flix.dto.security.UserPrincipal;
import com.fu.flix.service.ConfirmedUserService;
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

import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.Collection;

import static com.fu.flix.constant.Constant.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class ConfirmedUserServiceImplTest {

    @Autowired
    ConfirmedUserService confirmedUserService;

    @Autowired
    CommentDAO commentDAO;

    @Test
    public void test_comment_success_with_rating_is_5() {
        // given
        String requestCode = "030722WGR4WV";
        Integer rating = 5;
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(rating);
        request.setComment(comment);
        setContextUsername(48L, "0962706248");

        // when
        ResponseEntity<CommentResponse> responseEntity = confirmedUserService.createComment(request);
        CommentResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(COMMENT_SUCCESS, response.getMessage());
        clearCustomerComment(requestCode);
    }

    @Test
    public void test_comment_success_with_rating_is_1() {
        // given
        String requestCode = "030722WGR4WV";
        Integer rating = 1;
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(rating);
        request.setComment(comment);
        setContextUsername(48L, "0962706248");

        // when
        ResponseEntity<CommentResponse> responseEntity = confirmedUserService.createComment(request);
        CommentResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(COMMENT_SUCCESS, response.getMessage());
        clearCustomerComment(requestCode);
    }

    @Test
    public void test_comment_success_with_rating_is_4() {
        // given
        String requestCode = "030722WGR4WV";
        Integer rating = 4;
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(rating);
        request.setComment(comment);
        setContextUsername(48L, "0962706248");

        // when
        ResponseEntity<CommentResponse> responseEntity = confirmedUserService.createComment(request);
        CommentResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(COMMENT_SUCCESS, response.getMessage());
        clearCustomerComment(requestCode);
    }

    private void clearCustomerComment(String requestCode) {
        commentDAO.deleteByRequestCodeAndType(requestCode, CommentType.CUSTOMER_COMMENT.name());
    }

    @Test
    public void test_comment_fail_when_rating_is_6() {
        // given
        String requestCode = "030722WGR4WV";
        Integer rating = 6;
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(rating);
        request.setComment(comment);
        setContextUsername(48L, "0962706248");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> confirmedUserService.createComment(request));

        // then
        Assertions.assertEquals(RATING_MUST_IN_RANGE_1_TO_5, exception.getMessage());
    }

    @Test
    public void test_comment_fail_when_rating_is_0() {
        // given
        String requestCode = "030722WGR4WV";
        Integer rating = 0;
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(rating);
        request.setComment(comment);
        setContextUsername(48L, "0962706248");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> confirmedUserService.createComment(request));

        // then
        Assertions.assertEquals(RATING_MUST_IN_RANGE_1_TO_5, exception.getMessage());
    }

    @Test
    public void test_comment_fail_when_rating_is_null() {
        // given
        String requestCode = "030722WGR4WV";
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(null);
        request.setComment(comment);
        setContextUsername(48L, "0962706248");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> confirmedUserService.createComment(request));

        // then
        Assertions.assertEquals(RATING_IS_REQUIRED, exception.getMessage());
    }

    @Test
    public void test_comment_success_when_comment_is_null() {
        // given
        String requestCode = "030722WGR4WV";
        Integer rating = 4;
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(rating);
        request.setComment(null);
        setContextUsername(48L, "0962706248");

        // when
        ResponseEntity<CommentResponse> responseEntity = confirmedUserService.createComment(request);
        CommentResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(COMMENT_SUCCESS, response.getMessage());
        clearCustomerComment(requestCode);
    }

    @Test
    public void test_comment_success_when_comment_is_empty() {
        // given
        String requestCode = "030722WGR4WV";
        Integer rating = 4;
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(rating);
        request.setComment("");
        setContextUsername(48L, "0962706248");

        // when
        ResponseEntity<CommentResponse> responseEntity = confirmedUserService.createComment(request);
        CommentResponse response = responseEntity.getBody();

        // then
        Assertions.assertEquals(COMMENT_SUCCESS, response.getMessage());
        clearCustomerComment(requestCode);
    }

    @Test
    public void test_comment_fail_when_comment_length_is_251() {
        // given
        String requestCode = "030722WGR4WV";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(null);
        request.setComment("t".repeat(251));
        setContextUsername(48L, "0962706248");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> confirmedUserService.createComment(request));

        // then
        Assertions.assertEquals(EXCEEDED_COMMENT_LENGTH_ALLOWED, exception.getMessage());
    }

    @Test
    public void test_comment_fail_when_invalid_request_code() {
        // given
        String requestCode = "34AEFEMWQGNN";
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(4);
        request.setComment(comment);
        setContextUsername(48L, "0962706248");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> confirmedUserService.createComment(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_comment_fail_when_request_code_is_empty() {
        // given
        String requestCode = "";
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(requestCode);
        request.setRating(4);
        request.setComment(comment);
        setContextUsername(48L, "0962706248");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> confirmedUserService.createComment(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_comment_fail_when_request_code_is_null() {
        // given
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode(null);
        request.setRating(4);
        request.setComment(comment);
        setContextUsername(48L, "0962706248");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> confirmedUserService.createComment(request));

        // then
        Assertions.assertEquals(INVALID_REQUEST_CODE, exception.getMessage());
    }

    @Test
    public void test_comment_fail_when_request_code_and_user_does_not_match() {
        // given
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode("030722WGR4WV");
        request.setRating(4);
        request.setComment(comment);
        setContextUsername(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> confirmedUserService.createComment(request));

        // then
        Assertions.assertEquals(USER_AND_REQUEST_CODE_DOES_NOT_MATCH, exception.getMessage());
    }

    @Test
    public void test_comment_fail_when_comment_existed() {
        // given
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode("08AEFEMWQGWQ");
        request.setRating(4);
        request.setComment(comment);
        setContextUsername(36L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> confirmedUserService.createComment(request));

        // then
        Assertions.assertEquals(COMMENT_EXISTED, exception.getMessage());
    }

    @Test
    public void test_comment_fail_when_request_status_is_not_done() {
        // given
        String comment = "Thợ xấu trai";
        CommentRequest request = new CommentRequest();
        request.setRequestCode("030722NCUGTU");
        request.setRating(4);
        request.setComment(comment);
        setContextUsername(48L, "0865390037");

        // when
        Exception exception = Assertions.assertThrows(GeneralException.class, () -> confirmedUserService.createComment(request));

        // then
        Assertions.assertEquals(CAN_NOT_COMMENT_WHEN_STATUS_NOT_DONE, exception.getMessage());
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