package com.fu.flix.service.impl;


import com.fu.flix.configuration.AppConf;
import com.fu.flix.constant.Constant;
import com.fu.flix.dao.ImageDAO;
import com.fu.flix.dao.UserDAO;
import com.fu.flix.dto.request.UpdateAvatarRequest;
import com.fu.flix.dto.response.UpdateAvatarResponse;
import com.fu.flix.entity.Image;
import com.fu.flix.entity.User;
import com.fu.flix.service.CloudStorageService;
import com.fu.flix.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;

@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    private final ImageDAO imageDAO;
    private final CloudStorageService cloudStorageService;
    private final UserDAO userDAO;
    private final AppConf appConf;

    public UserServiceImpl(ImageDAO imageDAO,
                           CloudStorageService cloudStorageService,
                           UserDAO userDAO,
                           AppConf appConf) {
        this.imageDAO = imageDAO;
        this.cloudStorageService = cloudStorageService;
        this.userDAO = userDAO;
        this.appConf = appConf;
    }

    @Override
    public ResponseEntity<UpdateAvatarResponse> updateAvatar(UpdateAvatarRequest request) throws IOException {
        MultipartFile avatar = request.getAvatar();
        User user = userDAO.findByUsername(request.getUsername()).get();
        Image oldImage = imageDAO.findById(user.getAvatar()).get();

        if (avatar != null) {
            String url = cloudStorageService.uploadImage(avatar);

            if (isDefaultAvatar(oldImage)) {
                user = addNewAvatarToUser(user, url);
                userDAO.save(user);
            } else {
                oldImage.setUrl(url);
            }
        }

        UpdateAvatarResponse response = new UpdateAvatarResponse();
        response.setMessage(Constant.UPDATE_AVATAR_SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public User addNewAvatarToUser(User user, String url) {
        Image newImage = new Image();
        newImage.setName(user.getFullName());
        newImage.setUrl(url);
        Image savedImage = imageDAO.save(newImage);
        user.setAvatar(savedImage.getId());
        return user;
    }

    private boolean isDefaultAvatar(Image image) {
        return image.getId().equals(appConf.getDefaultAvatar());
    }
}
