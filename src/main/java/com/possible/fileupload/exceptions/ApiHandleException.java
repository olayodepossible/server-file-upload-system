package com.possible.fileupload.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@ControllerAdvice
public class ApiHandleException {
    @ExceptionHandler(ApiRequestException.class)
    public ResponseEntity<Object> handleApiRequestException(ApiRequestException e){
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ApiException apiException = new ApiException(e.getMessage(), badRequest, ZonedDateTime.now(ZoneId.of("Z")));
        return new ResponseEntity<>(apiException, badRequest);
    }

    @ExceptionHandler(FileNotFoundException.class)
    @ResponseBody
    public ApiException handleEntityNotFoundException(FileNotFoundException e){
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        return new ApiException(e.getMessage(), badRequest, ZonedDateTime.now(ZoneId.of("Z")));
    }


}
