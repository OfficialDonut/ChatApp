package us.donut.chat.core.request;

import us.donut.chat.core.Request;
import us.donut.chat.core.response.LeaveSessionResponse;

import java.util.UUID;

public class LeaveSessionRequest extends Request<LeaveSessionResponse> {

    private UUID sessionUUID;

    public LeaveSessionRequest(UUID sessionUUID) {
        this.sessionUUID = sessionUUID;
    }

    public UUID getSessionUUID() {
        return sessionUUID;
    }
}
