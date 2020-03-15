package us.donut.chat.client;

import javafx.application.Platform;
import us.donut.chat.core.ChatCore;
import us.donut.chat.core.ChatMessage;
import us.donut.chat.core.Request;
import us.donut.chat.core.Response;
import us.donut.chat.core.response.ExceptionResponse;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ChatClient {

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Map<UUID, CompletableFuture<Response>> requestFutures = new ConcurrentHashMap<>();
    private List<Consumer<ChatMessage>> listenerActions = new ArrayList<>();

    private static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public ChatClient() throws IOException {
        try (var trustStoreStream = getClass().getResourceAsStream("/ChatTrustStore.jks")) {
            var trustStoreFile = Files.createTempFile("ChatTrustStore", ".jks");
            trustStoreFile.toFile().deleteOnExit();
            Files.copy(trustStoreStream, trustStoreFile, StandardCopyOption.REPLACE_EXISTING);
            System.setProperty("javax.net.ssl.trustStore", trustStoreFile.toString());
        }

        socket = SSLSocketFactory.getDefault().createSocket(ChatCore.HOST, ChatCore.PORT);
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());

        executorService.submit(() -> {
            while (!socket.isClosed()) {
                try {
                    var object = inputStream.readObject();
                    if (object instanceof Response) {
                        var response = (Response) object;
                        var future = requestFutures.remove(response.getRequestUUID());
                        if (future != null) {
                            Platform.runLater(() -> {
                                if (response instanceof ExceptionResponse) {
                                    future.completeExceptionally(((ExceptionResponse) response).getException());
                                } else {
                                    future.complete(response);
                                }
                            });
                        }
                    } else if (object instanceof ChatMessage) {
                        for (Consumer<ChatMessage> action : listenerActions) {
                            Platform.runLater(() -> action.accept((ChatMessage) object));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException ignored) {}
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends Response> CompletableFuture<T> sendRequest(Request<T> request) {
        var future = new CompletableFuture<T>();
        requestFutures.put(request.getUUID(), (CompletableFuture<Response>) future);
        executorService.execute(() -> {
            try {
                outputStream.writeObject(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return future;
    }

    public void sendMessage(ChatMessage message) {
        executorService.execute(() -> {
            try {
                outputStream.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void addListener(Consumer<ChatMessage> action) {
        listenerActions.add(action);
    }

    public void disconnect() throws IOException {
        socket.close();
        outputStream.close();
        inputStream.close();
        executorService.shutdown();
    }
}
