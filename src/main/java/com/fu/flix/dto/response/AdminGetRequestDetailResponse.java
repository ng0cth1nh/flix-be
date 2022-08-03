package com.fu.flix.dto.response;

import com.fu.flix.dto.AccessoryOutputDTO;
import com.fu.flix.dto.ExtraServiceOutputDTO;
import com.fu.flix.dto.SubServiceOutputDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminGetRequestDetailResponse {
    private String requestCode;
    private String customerName;
    private String customerPhone;
    private String repairerName;
    private String repairerPhone;
    private String status;
    private String customerAddress;
    private String description;
    private String serviceName;
    private List<SubServiceOutputDTO> subServices;
    private List<AccessoryOutputDTO> accessories;
    private List<ExtraServiceOutputDTO> extraServices;
    private String voucherCode;
    private String voucherDiscount;
    private String voucherDescription;
    private String expectedFixingTime;
    private String paymentMethod;
    private String cancelReason;
    private String createdAt;
    private Long totalPrice;
    private Long vatPrice;
    private Long actualPrice;
    private Long totalDiscount;
    private Long inspectionPrice;
    private Long totalSubServicePrice;
    private Long totalAccessoryPrice;
    private Long totalExtraServicePrice;
}
