package com.fu.flix.dto.request;

import lombok.Data;

@Data
public class RegisterRequest extends DataRequest{
    private String firstName;
    private String lastName;
    private String phone;
    private String password;
}
