package com.fu.flix.entity;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class UserVoucherId implements Serializable {
    private Long userId;

    private Long voucherId;
}
