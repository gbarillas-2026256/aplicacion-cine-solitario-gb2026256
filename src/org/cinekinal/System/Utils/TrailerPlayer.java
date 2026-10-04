package org.cinekinal.system.utils;

import java.io.File;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Plays a local trailer video file in a modal popup window.
 * Replaces opening YouTube links in the system browser.
 */
public final class TrailerPlayer {

    private TrailerPlayer() {
    }

    /** Opens a modal window and plays the given local video file. */
    public static void play(String path) {
        if (path == null || path.isBlank()) {
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            new AlertInformation().viewAlert("WARNING", "TRÁILER NO ENCONTRADO",
                    "No se encontró el archivo del tráiler", path);
            return;
        }

        Media media = new Media(file.toURI().toString());
        MediaPlayer player = new MediaPlayer(media);
        MediaView view = new MediaView(player);
        view.setFitWidth(900);
        view.setPreserveRatio(true);

        Button btnClose = new Button("CERRAR");
        btnClose.getStyleClass().add("eva-button-ghost");
        StackPane.setAlignment(btnClose, Pos.TOP_RIGHT);

        StackPane root = new StackPane(view, btnClose);
        root.setStyle("-fx-background-color: black;");

        Stage stage = new Stage(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root, 920, 540));
        btnClose.setOnAction(e -> stage.close());
        stage.setOnHidden(e -> player.dispose());

        player.setOnReady(player::play);
        stage.showAndWait();
    }
}
