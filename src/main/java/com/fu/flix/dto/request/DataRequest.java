package com.fu.flix.dto.request;

import lombok.Data;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.fu.flix.constant.Constant.ANONYMOUS_USER;

@Data
public class DataRequest {
    public String getUsername() {
        String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        if (ANONYMOUS_USER.equals(username)) {
            return null;
        }

        return username;
    }
}
