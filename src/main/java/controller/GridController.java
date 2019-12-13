package controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import model.grid.hex.Hex;
import model.grid.hex.HexGridSearch;
import model.grid.hex.HexType;
import ui.ResizableCanvas;
import model.grid.hex.HexGrid;
import model.grid.hex.HexGrid.MapShape;
import util.SearchMethods;

public class GridController {

    private static final Color BACKGROUND = Color.GRAY;

    private static final double HEX_SIZE = 15.0D; //19

    private Stage stage;

    @FXML
    private StackPane wrappingPane;

    @FXML
    private ResizableCanvas canvasPane;

    private Canvas canvas;
    private GraphicsContext ctx;

    private HexGrid grid;
    private Hex start, end;

    private BooleanProperty finishedAnimation = new SimpleBooleanProperty(true);

    private boolean shiftHeld = false;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void initialize() {
        canvas = canvasPane.getCanvas();
        ctx = canvas.getGraphicsContext2D();
    }

    public void onStageShow() {
        stage.getScene().setOnKeyPressed(this::onKeyPressed);
        stage.getScene().setOnKeyReleased(this::onKeyReleased);

        Point2D size = new Point2D(HEX_SIZE, HEX_SIZE);
        Point2D dimensions = new Point2D(
            37, 25 //29,19
        );
        grid = new HexGrid(Point2D.ZERO, size, dimensions);
        redraw();

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setHeaderText("Instructions");
        info.setContentText(
            "- Left click drag to draw walls\n" +
                "- Middle click drag to draw dirt roads\n" +
                "- Right click drag to erase hex\n" +
                "- Shift + Left click to add starting point\n" +
                "- Shift + Right click to add ending point\n" +
                "- Press 'c' to clear (not reset) the grid\n" +
                "- Press (u)niform, (g)reedy, (a)star to perform searches\n" +
                "- Go to Edit -> Reset Graph to reset the grids"
        );
        info.getDialogPane().setStyle(
            "-fx-font-family: \"Segoe UI\";" +
                "-fx-font-size: 16px;"
        );
        info.showAndWait();
    }

    private Hex coordsToHex(double sceneX, double sceneY) {
        Point2D coords = canvasPane.sceneToLocal(sceneX, sceneY).subtract(
            new Point2D(canvas.getWidth() / 2, canvas.getHeight() / 2)
        );
        return grid.getHexAtCoordinates(coords);
    }

    private void redraw() {
        grid.populate(MapShape.RECTANGULAR);

        ctx.setLineWidth(1.0);
        ctx.setStroke(Color.WHITE);
        grid.draw(canvas, BACKGROUND);
    }

    @FXML
    private void onCanvasReset() {
        grid.reset();
        redraw();
    }

    @FXML
    private void onCanvasClear() {
        grid.draw(canvas, BACKGROUND);
    }

    @FXML
    private void onCanvasClicked(MouseEvent event) {
        if (!finishedAnimation.get()) {
            return;
        }
        if (!event.isStillSincePress()) {
            return;
        }
        MouseButton button = event.getButton();
        if (button != MouseButton.PRIMARY && button != MouseButton.SECONDARY &&
            button != MouseButton.MIDDLE
        ) {
            return;
        }
        Hex target = coordsToHex(event.getSceneX(), event.getSceneY());
        if (target == null) {
            return;
        }
        switch (button) {
            case PRIMARY:
                if (shiftHeld) {
                    if (start != null) {
                        ctx.setFill(BACKGROUND);
                        grid.fillHex(start, ctx);
                        grid.drawHex(start, ctx);
                        grid.setHexType(start, HexType.EMPTY);
                    }
                    ctx.setFill(HexGrid.COLORS.get(HexType.START));
                    grid.fillHex(target, ctx);
                    grid.setHexType(target, HexType.START);
                    start = target;
                } else {
                    if (target.equals(start)) {
                        start = null;
                    } else if (target.equals(end)) {
                        end = null;
                    }
                    ctx.setFill(HexGrid.COLORS.get(HexType.WALL));
                    grid.fillHex(target, ctx);
                    grid.setHexType(target, HexType.WALL);
                }
                break;
            case SECONDARY:
                if (shiftHeld) {
                    if (end != null) {
                        ctx.setFill(BACKGROUND);
                        grid.fillHex(end, ctx);
                        grid.drawHex(end, ctx);
                        grid.setHexType(end, HexType.EMPTY);
                    }
                    ctx.setFill(HexGrid.COLORS.get(HexType.END));
                    grid.fillHex(target, ctx);
                    grid.setHexType(target, HexType.END);
                    end = target;
                } else {
                    if (target.equals(start)) {
                        start = null;
                    } else if (target.equals(end)) {
                        end = null;
                    }
                    ctx.setFill(BACKGROUND);
                    grid.fillHex(target, ctx);
                    grid.drawHex(target, ctx);
                    grid.setHexType(target, HexType.EMPTY);
                }
                break;
            case MIDDLE:
                ctx.setFill(HexGrid.COLORS.get(HexType.DIRT));
                grid.fillHex(target, ctx);
                grid.setHexType(target, HexType.DIRT);
                break;
        }
    }

    @FXML
    private void onCanvasDragged(MouseEvent event) {
        if (!finishedAnimation.get()) {
            return;
        }
        MouseButton button = event.getButton();
        if (button != MouseButton.PRIMARY && button != MouseButton.SECONDARY &&
            button != MouseButton.MIDDLE
        ) {
            return;
        }
        Hex target = coordsToHex(event.getSceneX(), event.getSceneY());
        if (target == null) {
            return;
        }
        if (target.equals(start)) {
            start = null;
        } else if (target.equals(end)) {
            end = null;
        }
        switch (button) {
            case PRIMARY:
                ctx.setFill(HexGrid.COLORS.get(HexType.WALL));
                grid.fillHex(target, ctx);
                grid.setHexType(target, HexType.WALL);
                break;
            case SECONDARY:
                ctx.setFill(BACKGROUND);
                grid.fillHex(target, ctx);
                grid.drawHex(target, ctx);
                grid.setHexType(target, HexType.EMPTY);
                break;
            case MIDDLE:
                ctx.setFill(HexGrid.COLORS.get(HexType.DIRT));
                grid.fillHex(target, ctx);
                grid.setHexType(target, HexType.DIRT);
                break;
        }
    }

    private void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.SHIFT) {
            shiftHeld = true;
        }
    }

    @FXML
    private void uniformCost() {
        performSearch(SearchMethods.UCS);
    }

    @FXML
    private void greedySearch() {
        performSearch(SearchMethods.GREEDY);
    }

    @FXML
    private void aStarSearch() {
        performSearch(SearchMethods.A_STAR);
    }

    private void performSearch(String searchMethod) {
        if (start == null || end == null) {
            return;
        }
        if (!finishedAnimation.get()) {
            return;
        }
        finishedAnimation.set(false);
        long startTime = System.currentTimeMillis();
        HexGridSearch.search(grid, start, end, searchMethod, ctx, finishedAnimation);
        System.out.println(System.currentTimeMillis() - startTime);
    }

    private void onKeyReleased(KeyEvent event) {
        KeyCode key = event.getCode();
        switch (key) {
            case SHIFT:
                shiftHeld = false;
                return;
            case U:
                performSearch(SearchMethods.UCS);
                break;
            case G:
                performSearch(SearchMethods.GREEDY);
                break;
            case A:
                performSearch(SearchMethods.A_STAR);
                break;
            case C:
                onCanvasClear();
                break;
        }
    }
}
