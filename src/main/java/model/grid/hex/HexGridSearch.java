package model.grid.hex;

import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import util.SearchMethods;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class HexGridSearch {

    private enum HexMarkerTypes {
        START, END, FRINGE, CURRENT, PATH
    }
    private static final HashMap<HexMarkerTypes, Color> COLORS = new HashMap<>();
    static {
        COLORS.put(HexMarkerTypes.START, HexGrid.COLORS.get(HexType.START));
        COLORS.put(HexMarkerTypes.END, HexGrid.COLORS.get(HexType.END));
        COLORS.put(HexMarkerTypes.FRINGE, Color.web("#6688cc"));
        COLORS.put(HexMarkerTypes.CURRENT, Color.web("#d6e87d"));
        COLORS.put(HexMarkerTypes.PATH, Color.ORANGE);
    }

    private static final Duration PAUSE_DURATION = Duration.millis(20);

    private static Transition createColorTransition(HexGrid grid, Hex hex, Color color,
                                             GraphicsContext ctx) {
        PauseTransition transition = new PauseTransition(PAUSE_DURATION);
        transition.setOnFinished((event) -> {
            ctx.setFill(color);
            grid.fillHex(hex, ctx);
        });
        return transition;
    }

    public static void search(HexGrid grid, Hex start, Hex end, String method,
                              GraphicsContext ctx, BooleanProperty finished) {
        if (start == null || end == null) {
            throw new IllegalArgumentException(
                "Invalid arguments for search. Start: " + start + " | Goal: " + end);
        }

        SequentialTransition animation = new SequentialTransition();

        HashMap<Hex, Double> currentPathDist = new HashMap<>();
        HashMap<Hex, Double> heuristic = new HashMap<>();
        HashMap<Hex, Hex> parent = new HashMap<>();

        // Change comparator depending on search method
        Comparator<Hex> comparator;
        switch (method) {
            case SearchMethods.UCS:
                // Only use g(x) - path cost
                comparator = Comparator.comparingDouble(
                    currentPathDist::get
                );
                break;
            case SearchMethods.GREEDY:
                // Only use h(x) - heuristic
                comparator = Comparator.comparingDouble(
                    heuristic::get
                );
                break;
            case SearchMethods.A_STAR:
                // Use both g(x) & h(x)
                comparator = Comparator.comparingDouble(
                    (o) -> currentPathDist.get(o) + heuristic.get(o)
                );
                break;
            default:
                throw new IllegalArgumentException("Invalid search method " + method);
        }
        PriorityQueue<Hex> queue = new PriorityQueue<>(comparator);

        // Add initial node to the queue
        currentPathDist.put(start, 0.0D);
        queue.add(start);

        Set<Hex> explored = new HashSet<>();
        Hex goal = null;
        while (!queue.isEmpty()) {
            Hex current = queue.poll();

            Color c = COLORS.get(HexMarkerTypes.CURRENT);
            if (grid.getHexType(current) == HexType.DIRT) {
                c = c.darker();
            }
            animation.getChildren().add(
                createColorTransition(grid, current, c, ctx)
            );

            // The goal has been reached! (Lowest priority in the queue)
            if (current.equals(end)) {
                // If the goal does not have a proper cost, the goal is
                // unreachable
                if (currentPathDist.getOrDefault(current, Double.POSITIVE_INFINITY)
                    != Double.POSITIVE_INFINITY) {
                    goal = current;
                }
                break;
            }
            // Add the current node to the explored list
            explored.add(current);
            // Loop through all the edges
            for (Hex neighbor : grid.getNeighbors(current)) {
                // Only expand unexplored nodes
                if (!explored.contains(neighbor)) {
                    Color fc = COLORS.get(HexMarkerTypes.FRINGE);
                    if (grid.getHexType(neighbor) == HexType.DIRT) {
                        fc = fc.darker();
                    }
                    animation.getChildren().add(
                        createColorTransition(grid, neighbor, fc, ctx)
                    );
                    // Current cost + edge cost
                    double newDist = currentPathDist.getOrDefault(current,
                        Double.POSITIVE_INFINITY) + grid.getMovementCost(current, neighbor);
                    // Previous lowest cost through the node
                    double currentDist = currentPathDist.getOrDefault(neighbor,
                        Double.POSITIVE_INFINITY);
                    // If a shorter distance is found...
                    if (newDist < currentDist) {
                        // Update the new shorter distance
                        currentPathDist.put(neighbor, newDist);
                        heuristic.put(neighbor, grid.getStraightDistance(neighbor, end));
                        parent.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }
        ParallelTransition pathTransition = new ParallelTransition();
        List<Hex> path = new ArrayList<>();
        double cost;
        if (goal != null) {
            // Reconstruct path
            cost = currentPathDist.get(goal);
            while (goal != null) {
                path.add(0, goal);
                pathTransition.getChildren().add(
                    createColorTransition(grid, goal,
                        COLORS.get(HexMarkerTypes.PATH), ctx)
                );
                goal = parent.get(goal);
            }
        } else {
            cost = Double.POSITIVE_INFINITY;
        }
        pathTransition.getChildren().addAll(
            createColorTransition(grid, start,
                COLORS.get(HexMarkerTypes.START), ctx),
            createColorTransition(grid, end,
                COLORS.get(HexMarkerTypes.END), ctx)
        );
        animation.getChildren().add(pathTransition);
        animation.setOnFinished((event -> {
            finished.set(true);
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setHeaderText("Results");
            info.setContentText("Cost: " + cost);
            info.getDialogPane().setStyle(
                "-fx-font-family: \"Segoe UI\";" +
                "-fx-font-size: 16px;");
            Platform.runLater(info::showAndWait);
        }));
        animation.play();
        System.out.println("Cost: " + cost);
        System.out.println("Path: " + path);
    }
}
