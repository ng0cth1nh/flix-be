package com.fu.flix.dto.response;

import com.fu.flix.dto.RepairerCommentDTO;
import lombok.Data;

import java.util.List;

@Data
public class RepairerCommentResponse {
    private List<RepairerCommentDTO> repairerComments;
    private long totalRecord;
}
