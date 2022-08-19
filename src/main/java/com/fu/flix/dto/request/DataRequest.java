package com.fu.flix.dto.request;

import com.fu.flix.dto.security.UserPrincipal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Getter
@Setter
public abstract class DataRequest {
    public String getUsername() {
        if (SecurityContextHolder.getContext() != null) {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (principal != null) {
                    return principal.equals("anonymousUser") ? null : ((UserPrincipal) principal).getUsername();
                }
            }
        }

        return null;
    }

    public Long getUserId() {
        if (SecurityContextHolder.getContext() != null) {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (principal != null) {
                    return principal.equals("anonymousUser") ? null : ((UserPrincipal) principal).getId();
                }
            }
        }

        return null;
    }

    public List<String> getRoles() {
        if (SecurityContextHolder.getContext() != null) {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (principal != null) {
                    return principal.equals("anonymousUser") ? null : ((UserPrincipal) principal).getRoles();
                }
            }
        }

        return null;
    }
}
