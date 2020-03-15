package us.donut.chat.core.response;

import us.donut.chat.core.Response;

import java.util.UUID;

public class JoinSessionResponse extends Response {

    public JoinSessionResponse(UUID requestUUID) {
        super(requestUUID);
    }
}
