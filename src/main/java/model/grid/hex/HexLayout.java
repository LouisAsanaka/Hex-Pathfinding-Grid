package model.grid.hex;

import javafx.geometry.Point2D;
import model.grid.Layout;
import util.IntPoint2D;

import java.util.ArrayList;
import java.util.List;

public class HexLayout extends Layout {

    private HexOrientation orientation;
    private Point2D size;
    private Point2D origin;

    public HexLayout(HexOrientation orientation, Point2D size, Point2D origin) {
        this.orientation = orientation;
        this.size = size;
        this.origin = origin;
    }

    public Point2D getSize() {
        return size;
    }

    public Point2D toPixel(Hex hex) {
        final HexOrientation M = orientation;
        double x = (M.f0 * hex.q() + M.f1 * hex.r()) * size.getX();
        double y = (M.f2 * hex.q() + M.f3 * hex.r()) * size.getY();
        return new Point2D(x + origin.getX(), y + origin.getY());
    }

    public FractionalHex toHex(Point2D coords) {
        Point2D axial = getAxialCoordinate(coords);
        return new FractionalHex(axial.getX(), axial.getY());
    }

    public IntPoint2D getRoundedAxialCoordinate(Point2D coords) {
        return IntPoint2D.fromDoublePoint(
            FractionalHex.round(getAxialCoordinate(coords))
        );
    }

    public Point2D getAxialCoordinate(Point2D coords) {
        final HexOrientation M = orientation;
        Point2D pt = new Point2D(
            (coords.getX() - origin.getX()) / size.getX(),
            (coords.getY() - origin.getY()) / size.getY());
        double q = M.b0 * pt.getX() + M.b1 * pt.getY();
        double r = M.b2 * pt.getX() + M.b3 * pt.getY();
        return new Point2D(q, r);
    }

    public Point2D cornerOffset(int corner) {
        double angle = 2.0 * Math.PI *
            (orientation.startAngle + corner) / 6;
        return new Point2D(size.getX() * Math.cos(angle),
            size.getY() * Math.sin(angle));
    }

    public List<Point2D> corners(Hex hex) {
        List<Point2D> list = new ArrayList<>();
        Point2D center = toPixel(hex);
        for (int i = 0; i < 6; i++) {
            Point2D offset = cornerOffset(i);
            list.add(center.add(offset));
        }
        return list;
    }

    public HexOrientation getOrientation() {
        return orientation;
    }
}
