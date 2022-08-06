package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectCVRequest extends DataRequest {
    private Long repairerId;
    private String reason;
    private String rejectStatus;
}
