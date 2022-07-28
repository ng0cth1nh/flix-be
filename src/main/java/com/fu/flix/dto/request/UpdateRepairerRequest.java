package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRepairerRequest extends DataRequest {
    private String email;
    private String experienceDescription;
    private String communeId;
    private String streetAddress;
}
