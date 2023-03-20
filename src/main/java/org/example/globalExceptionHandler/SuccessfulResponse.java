package org.example.globalExceptionHandler;

public class SuccessfulResponse implements Response {
    public ResponseStatus getStatus() {
        return ResponseStatus.SUCCESS;
    }
}
