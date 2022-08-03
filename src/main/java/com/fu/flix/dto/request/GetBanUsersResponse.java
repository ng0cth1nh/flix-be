package com.fu.flix.dto.request;

import com.fu.flix.dto.BanUserDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetBanUsersResponse {
    private List<BanUserDTO> userList;
    private long totalRecord;
}
