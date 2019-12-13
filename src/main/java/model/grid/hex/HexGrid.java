package model.grid.hex;

import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import util.IntPoint2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HexGrid {

    public enum MapShape {
        HEXAGONAL, RECTANGULAR
    }

    public static final HashMap<HexType, Color> COLORS = new HashMap<>();
    static {
        COLORS.put(HexType.WALL, Color.MEDIUMBLUE);
        COLORS.put(HexType.DIRT, Color.SANDYBROWN.darker());
        COLORS.put(HexType.START, Color.GREEN);
        COLORS.put(HexType.END, Color.RED);
    }

    private HashMap<IntPoint2D, Hex> map = new HashMap<>();
    private HashMap<IntPoint2D, HexType> hexTypes = new HashMap<>();
    private HexLayout layout;

    private int mapWidth, mapHeight;

    public HexGrid(Point2D size, Point2D dimensions) {
        this(Point2D.ZERO, size, dimensions);
    }

    public HexGrid(Point2D origin, Point2D size, Point2D dimensions) {
        layout = new HexLayout(HexOrientation.POINTY, size, origin);
        mapWidth = (int) dimensions.getX();
        mapHeight = (int) dimensions.getY();
    }

    public void populate(MapShape shape) {
        map.clear();
        switch (shape) {
            case HEXAGONAL:
                for (int q = -mapWidth; q <= mapWidth; q++) {
                    int r1 = Math.max(-mapWidth, -q - mapWidth);
                    int r2 = Math.min(mapWidth, -q + mapWidth);
                    for (int r = r1; r <= r2; r++) {
                        map.put(new IntPoint2D(q, r), new Hex(q, r));
                    }
                }
                break;
            case RECTANGULAR:
                HexOrientation o = layout.getOrientation();
                if (o.equals(HexOrientation.POINTY)) {
                    int qStart = -Math.floorDiv(mapWidth, 2);
                    int qEnd = qStart + mapWidth;
                    int rStart = -Math.floorDiv(mapHeight, 2);
                    int rEnd = rStart + mapHeight;
                    for (var r = rStart; r < rEnd; r++) {
                        var rOffset = -Math.floorDiv(r, 2);
                        for (var q = qStart + rOffset; q < qEnd + rOffset; q++) {
                            map.put(new IntPoint2D(q, r), new Hex(q, r));
                        }
                    }
                } else if (o.equals(HexOrientation.FLAT)) {
                    for (int q = 0; q < mapWidth; q++) {
                        int q_offset = q >> 1; // or q>>1
                        for (int r = -q_offset; r < mapHeight - q_offset; r++) {
                            map.put(new IntPoint2D(q, r), new Hex(q, r));
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    public void reset() {
        map.clear();
        hexTypes.clear();
    }

    public void draw(Canvas canvas, Color background) {
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.setTransform(new Affine());
        ctx.setFill(background);
        ctx.fillRect(0, 0,
            canvas.getWidth(), canvas.getHeight());
        ctx.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);
        for (Hex hex : map.values()) {
            drawHex(hex, ctx);
            Color hexColor = COLORS.getOrDefault(getHexType(hex), null);
            if (hexColor != null) {
                ctx.setFill(hexColor);
                fillHex(hex, ctx);
            }
        }
    }

    public void drawHex(Hex hex, GraphicsContext ctx) {
        List<Point2D> corners = layout.corners(hex);
        double[] x = new double[6];
        double[] y = new double[6];
        for (int i = 0; i < 6; ++i) {
            Point2D corner = corners.get(i);
            x[i] = corner.getX();
            y[i] = corner.getY();
        }
        ctx.strokePolygon(x, y, 6);
    }

    public void drawText(Hex hex, GraphicsContext ctx) {
        int pointSize = (int) Math.round(0.4 * Math.min(
            Math.abs(layout.getSize().getX()), Math.abs(layout.getSize().getY())));
        Point2D center = layout.toPixel(hex);
        ctx.setFill(Color.PINK);
        ctx.setFont(Font.font("Segoe UI", pointSize));
        ctx.setTextAlign(TextAlignment.CENTER);
        ctx.setTextBaseline(VPos.CENTER);
        ctx.fillText((hex.q() + "," + hex.r() + "," + hex.s()), center.getX(), center.getY());
    }

    public void fillHex(Hex hex, GraphicsContext ctx) {
        List<Point2D> corners = layout.corners(hex);
        double[] x = new double[6];
        double[] y = new double[6];
        for (int i = 0; i < 6; ++i) {
            Point2D corner = corners.get(i);
            x[i] = corner.getX();
            y[i] = corner.getY();
        }
        ctx.fillPolygon(x, y, 6);
    }

    public boolean hasHexAt(IntPoint2D point) {
        return map.containsKey(point);
    }

    public Hex getHexAt(IntPoint2D point) {
        return map.getOrDefault(point, null);
    }

    public boolean hasHexAtCoordinates(Point2D coords) {
        return map.containsKey(layout.getRoundedAxialCoordinate(coords));
    }

    public Hex getHexAtCoordinates(Point2D coords) {
        return getHexAt(layout.getRoundedAxialCoordinate(coords));
    }

    public List<Hex> getNeighbors(Hex hex) {
        List<Hex> neighbors = new ArrayList<>();
        IntPoint2D hexPoint = hex.getPoint();
        for (int i = 0; i < 6; ++i) {
            IntPoint2D neighborLocation = hexPoint.add(Hex.POINT_DIRECTIONS[i]);
            Hex neighbor = getHexAt(neighborLocation);
            if (neighbor != null && getHexType(neighbor) != HexType.WALL) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    public double getMovementCost(Hex from, Hex to) {
        if (getHexType(to) == HexType.DIRT) {
            return 3.0;
        }
        return 1.0;
    }

    public double getStraightDistance(Hex from, Hex to) {
        return from.distanceTo(to);
    }

    public HexType getHexType(IntPoint2D point) {
        return hexTypes.getOrDefault(point, HexType.EMPTY);
    }

    public HexType getHexType(Hex hex) {
        return getHexType(hex.getPoint());
    }

    public void setHexType(IntPoint2D point, HexType type) {
        hexTypes.put(point, type);
    }

    public void setHexType(Hex hex, HexType type) {
        setHexType(hex.getPoint(), type);
    }
}
