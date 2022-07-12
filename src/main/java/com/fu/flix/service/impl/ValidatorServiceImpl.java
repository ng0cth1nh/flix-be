package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.Constant;
import com.fu.flix.dao.UserDAO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.entity.User;
import com.fu.flix.service.ValidatorService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.fu.flix.constant.Constant.PAGE_NUMBER_MUST_BE_GREATER_OR_EQUAL_0;
import static com.fu.flix.constant.Constant.PAGE_SIZE_MUST_BE_GREATER_OR_EQUAL_0;

@Service
public class ValidatorServiceImpl implements ValidatorService {
    private final UserDAO userDAO;
    private final AppConf appConf;

    public ValidatorServiceImpl(UserDAO userDAO,
                                AppConf appConf) {
        this.userDAO = userDAO;
        this.appConf = appConf;
    }

    @Override
    public User getUserValidated(String username) {
        if (username == null || username.isEmpty()) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, Constant.USER_NAME_IS_REQUIRED);
        }

        Optional<User> optionalUser = userDAO.findByUsername(username);
        return getUser(optionalUser);
    }

    @Override
    public User getUserValidated(Long userId) {
        if (userId == null) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, Constant.USER_ID_IS_REQUIRED);
        }

        Optional<User> optionalUser = userDAO.findById(userId);
        return getUser(optionalUser);
    }

    private User getUser(Optional<User> optionalUser) {
        if (optionalUser.isEmpty()) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, Constant.ACCOUNT_NOT_FOUND);
        }

        User user = optionalUser.get();
        if (!user.getIsActive()) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, Constant.USER_IS_INACTIVE);
        }
        return user;
    }

    @Override
    public Integer getPageSize(Integer pageSize) {
        pageSize = pageSize != null
                ? pageSize
                : this.appConf.getDefaultPageSize();
        if (pageSize < 0) {
            throw new GeneralException(HttpStatus.GONE, PAGE_SIZE_MUST_BE_GREATER_OR_EQUAL_0);
        }
        return pageSize;
    }

    @Override
    public Integer getPageNumber(Integer pageNumber) {
        pageNumber = pageNumber != null
                ? pageNumber
                : this.appConf.getDefaultPageNumber();
        if (pageNumber < 0) {
            throw new GeneralException(HttpStatus.GONE, PAGE_NUMBER_MUST_BE_GREATER_OR_EQUAL_0);
        }
        return pageNumber;
    }
}
