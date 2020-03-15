package us.donut.chat.server;

import us.donut.chat.core.ChatMessage;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatSession {

    private Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private UUID uuid = UUID.randomUUID();

    public ChatSession() {
        ChatServer.SESSIONS.put(uuid, this);
    }

    public boolean add(ClientHandler client) {
        if (clients.add(client)) {
            message(new ChatMessage(uuid, ChatServer.PREFIX + client + " has connected."));
            return true;
        }
        return false;
    }

    public boolean remove(ClientHandler client) {
        if (clients.remove(client)) {
            if (clients.isEmpty()) {
                ChatServer.SESSIONS.remove(uuid);
            } else {
                message(new ChatMessage(uuid, ChatServer.PREFIX + client + " has disconnected."));
            }
            return true;
        }
        return false;
    }

    public void message(ChatMessage message) {
        for (ClientHandler client : clients) {
            try {
                client.send(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Set<ClientHandler> getClients() {
        return Collections.unmodifiableSet(clients);
    }

    public UUID getUUID() {
        return uuid;
    }
}
