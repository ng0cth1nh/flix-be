package com.fu.flix.service.impl;

import com.fu.flix.dao.*;
import com.fu.flix.entity.Feedback;
import com.fu.flix.entity.Image;
import com.fu.flix.service.CloudStorageService;
import com.fu.flix.service.FeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class FeedbackServiceImpl implements FeedbackService {
    private final ImageDAO imageDAO;
    private final CloudStorageService cloudStorageService;


    public FeedbackServiceImpl(
            ImageDAO imageDAO,
            CloudStorageService cloudStorageService
    ) {
        this.imageDAO = imageDAO;
        this.cloudStorageService = cloudStorageService;
    }

    @Override
    public void postFeedbackImages(Feedback feedback, List<MultipartFile> images) throws IOException {
        if (!CollectionUtils.isEmpty(images)) {
            for (MultipartFile file : images) {
                String url = cloudStorageService.uploadImage(file);
                Image image = new Image();
                image.setName(feedback.getTitle());
                image.setUrl(url);
                Image savedImage = imageDAO.save(image);

                feedback.getImages().add(savedImage);
            }
        }
    }
}
