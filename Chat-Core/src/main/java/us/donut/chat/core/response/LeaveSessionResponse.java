package us.donut.chat.core.response;

import us.donut.chat.core.Response;

import java.util.UUID;

public class LeaveSessionResponse extends Response {

    public LeaveSessionResponse(UUID requestUUID) {
        super(requestUUID);
    }
}
