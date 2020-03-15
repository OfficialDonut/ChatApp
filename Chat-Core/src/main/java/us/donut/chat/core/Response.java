package us.donut.chat.core;

import java.io.Serializable;
import java.util.UUID;

public abstract class Response implements Serializable {

    private UUID requestUUID;

    public Response(UUID requestUUID) {
        this.requestUUID = requestUUID;
    }

    public UUID getRequestUUID() {
        return requestUUID;
    }
}
