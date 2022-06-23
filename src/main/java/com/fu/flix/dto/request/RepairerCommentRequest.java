package com.fu.flix.dto.request;

import lombok.Data;

@Data
public class RepairerCommentRequest extends DataRequest {
    private Long repairerId;
    private Integer limit;
    private Integer offset;
}
