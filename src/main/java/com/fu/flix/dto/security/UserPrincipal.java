package com.fu.flix.dto.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserPrincipal {
    private Long id;
    private String username;
    private String[] roles;
}
