package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.dao.*;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.CreateFeedbackRequest;
import com.fu.flix.entity.Feedback;
import com.fu.flix.entity.Image;
import com.fu.flix.service.CloudStorageService;
import com.fu.flix.service.FeedbackService;
import com.fu.flix.util.InputValidation;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

import static com.fu.flix.constant.Constant.*;

@Service
@Transactional
public class FeedbackServiceImpl implements FeedbackService {
    private final ImageDAO imageDAO;
    private final CloudStorageService cloudStorageService;
    private final RepairRequestDAO repairRequestDAO;
    private final Long NAME_MAX_LENGTH;
    private final Long DESCRIPTION_MAX_LENGTH;

    public FeedbackServiceImpl(
            ImageDAO imageDAO,
            CloudStorageService cloudStorageService,
            AppConf appConf,
            RepairRequestDAO repairRequestDAO
    ) {
        this.imageDAO = imageDAO;
        this.cloudStorageService = cloudStorageService;
        this.NAME_MAX_LENGTH = appConf.getNameMaxLength();
        this.DESCRIPTION_MAX_LENGTH = appConf.getDescriptionMaxLength();
        this.repairRequestDAO = repairRequestDAO;
    }

    @Override
    public void validateCreateFeedbackInput(CreateFeedbackRequest request) {
        InputValidation.getFeedbackTypeValidated(request.getFeedbackType());

        String requestCode = request.getRequestCode();
        if (requestCode != null && repairRequestDAO.findByRequestCode(requestCode).isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_REQUEST_CODE);
        }

        String title = request.getTitle();
        if (Strings.isEmpty(title) || title.length() > NAME_MAX_LENGTH) {
            throw new GeneralException(HttpStatus.GONE, INVALID_TITLE);
        }

        String description = request.getDescription();
        if (Strings.isEmpty(description) || description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new GeneralException(HttpStatus.GONE, INVALID_DESCRIPTION);
        }
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
