package com.fu.flix.service;

import com.fu.flix.entity.User;

public interface ValidatorService {
    User getUserValidated(String username);
    User getUserValidated(Long userId);
}
