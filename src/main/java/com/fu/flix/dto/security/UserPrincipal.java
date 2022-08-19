package com.fu.flix.dto.security;

import com.fu.flix.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserPrincipal {
    private Long id;
    private String username;
    private List<String> roles;
}
