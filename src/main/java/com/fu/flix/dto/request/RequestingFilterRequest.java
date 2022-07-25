package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class RequestingFilterRequest extends DataRequest {
    private List<Long> serviceIds;
    private String locationId;
    private String locationType;
    private String startDate;
    private String endDate;
}
