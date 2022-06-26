package com.fu.flix.dto.request;

import com.fu.flix.dto.security.UserPrincipal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.context.SecurityContextHolder;

@Getter
@Setter
public class DataRequest {
    public String getUsername() {
        if (SecurityContextHolder.getContext() != null) {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null) {
                    return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
                }
            }
        }

        return null;
    }

    public Long getUserId() {
        if (SecurityContextHolder.getContext() != null) {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null) {
                    return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
                }
            }
        }

        return null;
    }
}
