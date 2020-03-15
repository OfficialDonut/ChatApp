package us.donut.chat.server;

import us.donut.chat.core.ChatCore;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class ChatServer {

    public static final Map<UUID, ChatSession> SESSIONS = new ConcurrentHashMap<>();
    public static final String PREFIX = "[SERVER] ";

    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.keyStore", Paths.get(args[0]).toString());
        System.setProperty("javax.net.ssl.keyStorePassword", args[1]);

        try {
            var serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(ChatCore.PORT);
            var executorService = Executors.newCachedThreadPool();

            executorService.submit(() -> {
                while (!serverSocket.isClosed()) {
                    try {
                        var clientSocket = serverSocket.accept();
                        executorService.submit(new ClientHandler(clientSocket));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
