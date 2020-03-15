package us.donut.chat.core.request;

import us.donut.chat.core.Request;
import us.donut.chat.core.response.JoinSessionResponse;

import java.util.UUID;

public class JoinSessionRequest extends Request<JoinSessionResponse> {

    private UUID sessionUUID;

    public JoinSessionRequest(UUID sessionUUID) {
        this.sessionUUID = sessionUUID;
    }

    public UUID getSessionUUID() {
        return sessionUUID;
    }
}
