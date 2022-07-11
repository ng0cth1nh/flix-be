package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddAccessoriesToInvoiceRequest extends DataRequest {
    private String requestCode;
    private List<Long> accessoryIds;
}
