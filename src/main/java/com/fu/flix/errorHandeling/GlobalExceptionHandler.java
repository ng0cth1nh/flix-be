package com.fu.flix.errorHandeling;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fu.flix.constant.Constant;
import com.fu.flix.dto.error.ErrorInfo;
import com.fu.flix.dto.error.GeneralException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

import static com.fu.flix.constant.Constant.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler({GeneralException.class})
    public ResponseEntity<Object> handleGeneralException(GeneralException e) {
        logger.error(e);
        ErrorInfo err = new ErrorInfo();
        err.setTimestamp(LocalDateTime.now());
        err.setStatus(e.getStatus());
        err.setCode(e.getRawStatusCode());
        err.setMessage(e.getMessage());
        err.setMessageParams(e.getMessageParams());
        return ResponseEntityBuilder.build(err);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException e, HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error(e);
        ErrorInfo err = new ErrorInfo();
        err.setTimestamp(LocalDateTime.now());
        err.setStatus(status);
        err.setCode(status.value());
        err.setMessage(WRONG_DATA_TYPE);
        return ResponseEntityBuilder.build(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnwantedException(Exception e) {
        logger.error(e);
        ErrorInfo err = new ErrorInfo();
        err.setTimestamp(LocalDateTime.now());
        err.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        err.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        err.setMessage(Constant.INTERNAL_SERVER_ERROR);
        return ResponseEntityBuilder.build(err);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Object> handleTokenExpiredException(TokenExpiredException e) {
        logger.error(e);
        ErrorInfo err = new ErrorInfo();
        err.setTimestamp(LocalDateTime.now());
        err.setStatus(HttpStatus.BAD_REQUEST);
        err.setCode(HttpStatus.BAD_REQUEST.value());
        err.setMessage(TOKEN_EXPIRED);
        return ResponseEntityBuilder.build(err);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        logger.error(e);
        ErrorInfo err = new ErrorInfo();
        err.setTimestamp(LocalDateTime.now());
        err.setStatus(HttpStatus.BAD_REQUEST);
        err.setCode(HttpStatus.BAD_REQUEST.value());
        err.setMessage(MAXIMUM_UPLOAD_SIZE_EXCEEDED);
        return ResponseEntityBuilder.build(err);
    }
}
