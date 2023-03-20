package org.example.globalExceptionHandler;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ErrorResponseBuilder {
    public Builder builder(){
        return new Builder();
    }

    @Data
    @Accessors(chain = true)
    public class Builder {
        private static final String ERROR_DESCRIPTION_HIDDEN = "Description was hidden. Contact support.";

        private String errorCode;
        private String description;
        private String message;

        private Builder() {
        }

        public ErrorResponse build() {
            return new ErrorResponse(buildErrorCode(), buildDescription(), buildMessage());
        }

        private String buildErrorCode() {
            if (StringUtils.isNotBlank(errorCode)) {
                return errorCode;
            } else {
                return CommonErrorCodes.GENERAL;
            }
        }

        private String buildDescription() {
            return description;
        }

        private String buildMessage() {
            if (StringUtils.isNotBlank(message)) {
                return message;
            }
            return "some error occurred";
        }
    }
}