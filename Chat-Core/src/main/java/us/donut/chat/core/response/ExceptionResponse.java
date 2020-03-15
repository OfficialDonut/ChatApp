package us.donut.chat.core.response;

import us.donut.chat.core.Response;

import java.util.UUID;

public class ExceptionResponse extends Response {

    private Exception exception;

    public ExceptionResponse(UUID requestUUID, Exception exception) {
        super(requestUUID);
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
