package vn.edu.ves.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * VES-Monitor Admin Desktop entry point.
 *
 * <p>5 màn theo §24.5 UPGRADE_PLAN: Login → Dashboard (4 pillar TabPane + Security Score)
 * → Region CRUD → AlertRule CRUD → User CRUD.</p>
 *
 * <p>Run: <code>mvn -pl desktop-admin javafx:run</code></p>
 */
public class MainApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);
    private static final String APP_TITLE = "VES-Monitor Admin Desktop";
    private static final String LOGIN_FXML = "/fxml/login.fxml";
    private static final String MATERIAL_CSS = "/css/material.css";

    @Override
    public void start(Stage stage) throws IOException {
        URL fxmlUrl = Objects.requireNonNull(
                getClass().getResource(LOGIN_FXML),
                "Không tìm thấy " + LOGIN_FXML + " trên classpath");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        Scene scene = new Scene(root, 480, 360);
        URL cssUrl = getClass().getResource(MATERIAL_CSS);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.setMinWidth(480);
        stage.setMinHeight(360);
        stage.show();

        log.info("MainApp started — opened {}", LOGIN_FXML);
    }

    public static void main(String[] args) {
        log.info("Bootstrapping {}", APP_TITLE);
        launch(args);
    }
}
