package us.donut.chat.core;

import java.io.Serializable;
import java.util.UUID;

public abstract class Request<T extends Response> implements Serializable {

    private UUID uuid = UUID.randomUUID();

    public UUID getUUID() {
        return uuid;
    }
}
