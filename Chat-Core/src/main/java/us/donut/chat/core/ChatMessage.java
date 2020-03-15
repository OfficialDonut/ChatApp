package us.donut.chat.core;

import java.io.Serializable;
import java.util.UUID;

public class ChatMessage implements Serializable {

    private UUID sessionUUID;
    private String message;

    public ChatMessage(UUID sessionUUID, String message) {
        this.sessionUUID = sessionUUID;
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getSessionUUID() {
        return sessionUUID;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}
