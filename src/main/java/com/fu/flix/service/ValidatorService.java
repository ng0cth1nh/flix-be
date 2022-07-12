package com.fu.flix.service;

import com.fu.flix.entity.Category;
import com.fu.flix.entity.Service;
import com.fu.flix.entity.User;

public interface ValidatorService {
    User getUserValidated(String username);

    User getUserValidated(Long userId);

    Integer getPageSize(Integer pageSize);

    Integer getPageNumber(Integer pageNumber);

    Service getServiceValidated(Long serviceId);

    Category getCategoryValidated(Long categoryId);
}
