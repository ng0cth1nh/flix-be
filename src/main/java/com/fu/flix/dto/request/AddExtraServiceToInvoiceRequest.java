package com.fu.flix.dto.request;

import com.fu.flix.dto.ExtraServiceDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddExtraServiceToInvoiceRequest extends DataRequest {
    private String requestCode;
    private List<ExtraServiceDTO> extraServices;
}
