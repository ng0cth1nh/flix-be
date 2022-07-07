package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddSubServicesToInvoiceRequest extends DataRequest {
    private String requestCode;
    private List<Long> subServiceIds;
}
