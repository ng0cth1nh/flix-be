package com.fu.flix.dto.request;

import lombok.Data;
import org.springframework.security.core.context.SecurityContextHolder;

@Data
public class DataRequest {
    public String getUsername() {
        if (SecurityContextHolder.getContext() != null) {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null) {
                    return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
                }
            }
        }

        return null;
    }
}
