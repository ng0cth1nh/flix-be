package com.fu.flix.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateAvatarRequest extends DataRequest{
    private MultipartFile avatar;
}
