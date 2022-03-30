package com.possible.fileupload.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class ApiException {
    private final  String message;
    private final HttpStatus status;
    private final ZonedDateTime timestamp;
}
