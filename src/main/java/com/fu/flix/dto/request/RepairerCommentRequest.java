package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepairerCommentRequest extends DataRequest {
    private Long repairerId;
    private Integer limit;
    private Integer offset;
}
