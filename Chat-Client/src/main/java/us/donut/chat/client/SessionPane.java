package us.donut.chat.client;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import us.donut.chat.core.ChatMessage;

import java.util.UUID;

public class SessionPane extends VBox {

    private UUID sessionUUID;

    public SessionPane(ChatClient chatClient, UUID sessionUUID) {
        getStyleClass().add("session-pane");
        this.sessionUUID = sessionUUID;
        var uuidField = new TextField(sessionUUID.toString());
        var uuidBox = new HBox(5, new Label("Session ID:"), uuidField);
        var messageArea = new TextArea();
        var messageField = new TextField();
        uuidBox.setAlignment(Pos.CENTER_LEFT);
        uuidField.setEditable(false);
        messageArea.setEditable(false);
        messageArea.prefHeightProperty().bind(heightProperty());
        Platform.runLater(messageField::requestFocus);
        getChildren().addAll(uuidBox, messageArea, messageField);

        messageField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                chatClient.sendMessage(new ChatMessage(sessionUUID, messageField.getText()));
                messageField.setText("");
            }
        });

        chatClient.addListener(message -> {
            if (sessionUUID.equals(message.getSessionUUID())) {
                if (!messageArea.getText().isEmpty()) {
                    messageArea.appendText("\n");
                }
                messageArea.appendText(message.toString());
            }
        });
    }

    public UUID getSessionUUID() {
        return sessionUUID;
    }
}
