package com.fu.flix.service.impl;

import com.fu.flix.constant.Constant;
import com.fu.flix.dao.UserDAO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.entity.User;
import com.fu.flix.service.ValidatorService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ValidatorServiceImpl implements ValidatorService {
    private final UserDAO userDAO;

    public ValidatorServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User getUserValidated(String username) {
        if (username == null || username.isEmpty()) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, Constant.USER_NAME_IS_REQUIRED);
        }

        Optional<User> optionalUser = userDAO.findByUsername(username);
        User user = getUser(optionalUser);
        return user;
    }

    @Override
    public User getUserValidated(Long userId) {
        if (userId == null) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, Constant.USER_ID_IS_REQUIRED);
        }

        Optional<User> optionalUser = userDAO.findById(userId);
        User user = getUser(optionalUser);
        return user;
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
}
