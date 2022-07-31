package com.fu.flix.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepairerTransactionDTO {
    private Long id;
    private Long amount;
    private String transactionCode;
    private String type;
    private String status;
    private String createdAt;
}
