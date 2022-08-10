package com.fu.flix.dto.response;

import com.fu.flix.dto.UserAddressDTO;
import lombok.Data;

import java.util.List;

@Data
public class UserAddressResponse {
    private List<UserAddressDTO> addresses;
}
