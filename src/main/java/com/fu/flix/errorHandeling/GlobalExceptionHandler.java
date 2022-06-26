package com.fu.flix.errorHandeling;

import com.fu.flix.dto.error.ErrorInfo;
import com.fu.flix.dto.error.GeneralException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({GeneralException.class})
    public ResponseEntity<Object> handleGeneralException(GeneralException e) {
        logger.error(e);
        ErrorInfo err = new ErrorInfo();
        err.setTimestamp(LocalDateTime.now());
        err.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        err.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        err.setMessage(e.getMessage());
        return ResponseEntityBuilder.build(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnwantedException(Exception e) {
        logger.error(e);
        ErrorInfo err = new ErrorInfo();
        err.setTimestamp(LocalDateTime.now());
        err.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        err.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        err.setMessage(e.getMessage());
        return ResponseEntityBuilder.build(err);
    }
}
