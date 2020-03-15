package us.donut.chat.server;

import us.donut.chat.core.ChatMessage;
import us.donut.chat.core.Request;
import us.donut.chat.core.request.CreateSessionRequest;
import us.donut.chat.core.request.JoinSessionRequest;
import us.donut.chat.core.request.LeaveSessionRequest;
import us.donut.chat.core.response.CreateSessionResponse;
import us.donut.chat.core.response.ExceptionResponse;
import us.donut.chat.core.response.JoinSessionResponse;
import us.donut.chat.core.response.LeaveSessionResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public ClientHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        inputStream = new ObjectInputStream(clientSocket.getInputStream());
    }

    @Override
    public void run() {
        while (!clientSocket.isClosed()) {
            try {
                var object = inputStream.readObject();
                if (object instanceof ChatMessage) {
                    ChatSession chatSession = ChatServer.SESSIONS.get(((ChatMessage) object).getSessionUUID());
                    if (chatSession != null) {
                        ChatMessage message = (ChatMessage) object;
                        message.setMessage("[" + getClientName() + "] " + message.getMessage());
                        chatSession.message(message);
                    }
                } else if (object instanceof Request) {
                    handleRequest((Request<?>) object);
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (ClassNotFoundException ignored) {}
        }

        ChatServer.SESSIONS.values().forEach(chatSession -> chatSession.remove(this));
        try {
            clientSocket.close();
            outputStream.close();
            inputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleRequest(Request<?> request) throws IOException {
        if (request instanceof CreateSessionRequest) {
            ChatSession chatSession = new ChatSession();
            chatSession.add(this);
            send(new CreateSessionResponse(request.getUUID(), chatSession.getUUID()));
        } else if (request instanceof JoinSessionRequest) {
            ChatSession chatSession = ChatServer.SESSIONS.get(((JoinSessionRequest) request).getSessionUUID());
            if (chatSession != null) {
                if (chatSession.add(this)) {
                    send(new JoinSessionResponse(request.getUUID()));
                } else {
                    send(new ExceptionResponse(request.getUUID(), new IllegalArgumentException("Already in this session")));
                }
            } else {
                send(new ExceptionResponse(request.getUUID(), new IllegalArgumentException("Invalid session ID")));
            }
        } else if (request instanceof LeaveSessionRequest) {
            ChatSession chatSession = ChatServer.SESSIONS.get(((LeaveSessionRequest) request).getSessionUUID());
            if (chatSession != null) {
                if (chatSession.remove(this)) {
                    send(new LeaveSessionResponse(request.getUUID()));
                } else {
                    send(new ExceptionResponse(request.getUUID(), new IllegalArgumentException("Not in this session")));
                }
            } else {
                send(new ExceptionResponse(request.getUUID(), new IllegalArgumentException("Invalid session ID")));
            }
        }
    }

    public synchronized void send(Object object) throws IOException {
        outputStream.writeObject(object);
    }

    public String getClientName() {
        return "@" + System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return getClientName();
    }
}
