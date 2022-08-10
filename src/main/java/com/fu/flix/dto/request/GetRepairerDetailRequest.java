package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetRepairerDetailRequest extends DataRequest {
    private Long repairerId;
}
