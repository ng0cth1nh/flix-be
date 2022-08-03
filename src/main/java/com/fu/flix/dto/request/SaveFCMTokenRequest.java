package com.fu.flix.dto.request;

import lombok.Data;

@Data
public class SaveFCMTokenRequest extends DataRequest{
    private String token;
}
