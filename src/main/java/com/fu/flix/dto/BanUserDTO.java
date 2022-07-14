package com.fu.flix.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BanUserDTO {
    private Long id;
    private String avatar;
    private String name;
    private String phone;
    private String role;
    private String banReason;
    private String banAt;
}
