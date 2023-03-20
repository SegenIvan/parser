package org.example.globalExceptionHandler;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ErrorResponse implements Response {
    private final Error error;

    public ErrorResponse(String code, String description, String message) {
        this.error = new Error(code, description, message);
    }

    public ResponseStatus getStatus() {
        return ResponseStatus.FAIL;
    }
}
