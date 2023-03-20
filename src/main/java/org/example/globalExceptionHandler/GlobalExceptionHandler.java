package org.example.globalExceptionHandler;


import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;


@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @Autowired
    private ErrorResponseBuilder errorResponseBuilder;

    /*@ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = errorResponseBuilder.builder()
                .setErrorCode(CommonErrorCodes.GENERAL)
                .setDescription(ex.getMessage())
                .setMessage("operation failed")
                .build();
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(GoogleJsonResponseException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = errorResponseBuilder.builder()
                .setErrorCode(CommonErrorCodes.GENERAL)
                .setDescription(ex.getMessage())
                .setMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }*/
}