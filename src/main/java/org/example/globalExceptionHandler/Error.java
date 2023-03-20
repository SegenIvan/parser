package org.example.globalExceptionHandler;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Error {
    private final String code;

    private final String description;

    private final String message;
}
