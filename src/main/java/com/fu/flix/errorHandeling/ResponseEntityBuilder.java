package com.fu.flix.errorHandeling;
import com.fu.flix.dto.error.ErrorInfo;
import org.springframework.http.ResponseEntity;


public class ResponseEntityBuilder {
    public static ResponseEntity<Object> build(ErrorInfo errorInfo) {
        return new ResponseEntity<>(errorInfo, errorInfo.getStatus());
    }
}