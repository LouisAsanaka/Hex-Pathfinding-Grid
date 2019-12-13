import controller.GridController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    public static final int WINDOW_WIDTH = 1000;
    public static final int WINDOW_HEIGHT = 600;
    public static final String WINDOW_TITLE = "Hexagonal Grid Pathfinding";

    @Override
    public void start(Stage stage) {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/view/scene.fxml")
        );
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error> Failed to load FXML file.");
            System.exit(1);
            return;
        }
        GridController controller = loader.getController();
        controller.setStage(stage);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.setTitle(WINDOW_TITLE);
        stage.getIcons().add(new Image("/images/icon.jpg"));
        stage.setResizable(false);
        stage.show();

        controller.onStageShow();
    }
}
