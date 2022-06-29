package com.fu.flix.service;

import com.fu.flix.dto.request.DataRequest;
import com.fu.flix.entity.User;

public interface UserValidatorService {
    User getUserValidated(String username);
    User getUserValidated(Long userId);
}
