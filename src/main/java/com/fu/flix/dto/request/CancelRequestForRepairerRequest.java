package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelRequestForRepairerRequest extends DataRequest {
    private String requestCode;
    private String reason;
}
