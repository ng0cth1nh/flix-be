package com.fpt.flix.flix_app.models.requests;

import lombok.Data;

@Data
public class RegisterCustomerRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String password;
}
