package com.fu.flix.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UpdateAvatarRequest extends DataRequest{
    private MultipartFile avatar;
}
