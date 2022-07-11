package com.fu.flix.service.impl;

import com.fu.flix.constant.Constant;
import com.fu.flix.dao.ImageDAO;
import com.fu.flix.dao.ServiceDAO;
import com.fu.flix.dto.IServiceDTO;
import com.fu.flix.dto.SearchServiceDTO;
import com.fu.flix.dto.ServiceDTO;
import com.fu.flix.dto.error.GeneralException;
import com.fu.flix.dto.request.SearchServicesRequest;
import com.fu.flix.dto.request.ServiceRequest;
import com.fu.flix.dto.request.ServiceResponse;
import com.fu.flix.dto.response.SearchServicesResponse;
import com.fu.flix.entity.Image;
import com.fu.flix.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fu.flix.constant.Constant.INVALID_KEY_WORD;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final ServiceDAO serviceDAO;
    private final ImageDAO imageDAO;

    public CategoryServiceImpl(ServiceDAO serviceDAO,
                               ImageDAO imageDAO) {
        this.serviceDAO = serviceDAO;
        this.imageDAO = imageDAO;
    }

    @Override
    public ResponseEntity<ServiceResponse> getServicesByCategory(ServiceRequest request) {
        Long categoryId = request.getCategoryId();
        if (categoryId == null) {
            throw new GeneralException(HttpStatus.GONE, Constant.INVALID_CATEGORY_ID);
        }

        List<com.fu.flix.entity.Service> services = serviceDAO.findByCategoryId(categoryId);
        if (services.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, Constant.INVALID_CATEGORY_ID);
        }

        List<ServiceDTO> serviceDTOS = services.stream()
                .map(service -> {
                    Optional<Image> optionalImage = imageDAO.findById(service.getImageId());
                    ServiceDTO dto = new ServiceDTO();
                    dto.setServiceId(service.getId());
                    dto.setPrice(service.getInspectionPrice());
                    dto.setImageUrl(optionalImage.map(Image::getUrl).orElse(null));
                    dto.setServiceName(service.getName());
                    return dto;
                }).collect(Collectors.toList());

        ServiceResponse response = new ServiceResponse();
        response.setServices(serviceDTOS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SearchServicesResponse> searchServices(SearchServicesRequest request) {
        String keyword = request.getKeyword();
        if (keyword == null || keyword.isEmpty()) {
            throw new GeneralException(HttpStatus.GONE, INVALID_KEY_WORD);
        }

        List<IServiceDTO> services = serviceDAO.searchServices(keyword);

        List<SearchServiceDTO> searchServiceDTOS = services.stream()
                .map(service -> {
                    SearchServiceDTO dto = new SearchServiceDTO();
                    dto.setServiceId(service.getServiceId());
                    dto.setServiceName(service.getServiceName());
                    dto.setIcon(service.getIcon());
                    dto.setImage(service.getImage());
                    return dto;
                }).collect(Collectors.toList());

        SearchServicesResponse response = new SearchServicesResponse();
        response.setServices(searchServiceDTOS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
