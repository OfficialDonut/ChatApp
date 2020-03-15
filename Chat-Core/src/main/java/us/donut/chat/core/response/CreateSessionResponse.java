package us.donut.chat.core.response;

import us.donut.chat.core.Response;

import java.util.UUID;

public class CreateSessionResponse extends Response {

    private UUID sessionUUID;

    public CreateSessionResponse(UUID requestUUID, UUID sessionUUID) {
        super(requestUUID);
        this.sessionUUID = sessionUUID;
    }

    public UUID getSessionUUID() {
        return sessionUUID;
    }
}
