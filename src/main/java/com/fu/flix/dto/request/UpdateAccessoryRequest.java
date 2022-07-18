package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAccessoryRequest extends ModifyAccessoryRequest {
    private Long id;
}
