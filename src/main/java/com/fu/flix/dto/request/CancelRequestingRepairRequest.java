package com.fu.flix.dto.request;

import lombok.Data;

@Data
public class CancelRequestingRepairRequest extends DataRequest {
    private String requestCode;
}
