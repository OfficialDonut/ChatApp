package us.donut.chat.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.lang3.exception.ExceptionUtils;
import us.donut.chat.core.request.CreateSessionRequest;
import us.donut.chat.core.request.JoinSessionRequest;
import us.donut.chat.core.request.LeaveSessionRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatApp extends Application {

    private BorderPane rootPane = new BorderPane();
    private Scene scene = new Scene(rootPane, 500, 500);
    private Stage stage;

    private ChatClient chatClient;
    private Map<UUID, SessionPane> sessions = new HashMap<>();
    private ComboBox<UUID> sessionBox = new ComboBox<>();

    @Override
    public void start(Stage stage) {
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> Platform.runLater(() -> displayException(e)));
        this.stage = stage;
        Platform.runLater(this::load);
    }

    @Override
    public void stop() throws IOException {
        if (chatClient != null) {
            chatClient.disconnect();
        }
    }

    private void load() {
        try {
            chatClient = new ChatClient();
            setupToolBar();
            scene.getStylesheets().add("/style.css");
            stage.setTitle("ChatApp");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            displayException(e);
            Platform.exit();
        }
    }

    private void setupToolBar() {
        sessionBox.valueProperty().addListener(((observableValue, oldValue, newValue) -> {
            rootPane.setCenter(sessions.get(sessionBox.getValue()));
            rootPane.requestFocus();
        }));

        var createButton = new Button("Create Session");
        createButton.setOnAction(e -> {
            var future = chatClient.sendRequest(new CreateSessionRequest());
            future.whenComplete((response, ex) -> {
                if (response != null) {
                    UUID uuid = response.getSessionUUID();
                    SessionPane sessionPane = new SessionPane(chatClient, uuid);
                    sessions.put(uuid, sessionPane);
                    sessionBox.getItems().add(uuid);
                    sessionBox.setValue(uuid);
                } else {
                    displayException(ex);
                }
            });
        });

        var joinButton = new Button("Join Session");
        joinButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Join Session");
            dialog.setContentText("Session ID:");
            dialog.setHeaderText(null);
            dialog.setGraphic(null);
            try {
                var uuid = UUID.fromString(dialog.showAndWait().orElse("").trim());
                var future = chatClient.sendRequest(new JoinSessionRequest(uuid));
                future.whenComplete((response, ex) -> {
                    if (response != null) {
                        SessionPane sessionPane = new SessionPane(chatClient, uuid);
                        sessions.put(uuid, sessionPane);
                        sessionBox.getItems().add(uuid);
                        sessionBox.setValue(uuid);
                    } else {
                        displayException(ex);
                    }
                });
            } catch (IllegalArgumentException ex) {
                displayException(ex);
            }
        });

        var leaveButton = new Button("Leave Session");
        leaveButton.setOnAction(e -> {
            Node node = rootPane.getCenter();
            if (node instanceof SessionPane) {
                UUID sessionUUID = ((SessionPane) node).getSessionUUID();
                sessionBox.getItems().remove(sessionUUID);
                chatClient.sendRequest(new LeaveSessionRequest(sessionUUID));
            }
        });

        var toolBar = new ToolBar(new Label("Session:"), sessionBox, createButton, joinButton, leaveButton);
        rootPane.setBottom(toolBar);
    }

    public static void displayException(Throwable e) {
        var alert = new Alert(Alert.AlertType.ERROR, null, ButtonType.CLOSE);
        alert.setHeaderText(null);
        alert.setGraphic(null);
        VBox content = new VBox(5, new Text(e.getMessage()), new TextArea(ExceptionUtils.getStackTrace(e)));
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
}
