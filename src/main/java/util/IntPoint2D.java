package util;

import javafx.geometry.Point2D;

public class IntPoint2D {

    public static final IntPoint2D ZERO = new IntPoint2D(0, 0);

    private int x, y;
    private int hash = 0;

    public IntPoint2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public final int getX() {
        return x;
    }

    public final int getY() {
        return y;
    }

    public double distance(int otherX, int otherY) {
        int dx = x - otherX;
        int dy = y - otherY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double distance(IntPoint2D point) {
        return distance(point.getX(), point.getY());
    }

    public IntPoint2D add(int otherX, int otherY) {
        return new IntPoint2D(x + otherX, y + otherY);
    }

    public IntPoint2D add(IntPoint2D other) {
        return add(other.getX(), other.getY());
    }

    public IntPoint2D subtract(int otherX, int otherY) {
        return new IntPoint2D(x - otherX, y - otherY);
    }

    public IntPoint2D subtract(IntPoint2D other) {
        return subtract(other.getX(), other.getY());
    }

    public IntPoint2D multiply(int k) {
        return new IntPoint2D(x * k, y * k);
    }

    public Point2D toDoublePoint() {
        return new Point2D(x, y);
    }

    public static IntPoint2D fromDoublePoint(Point2D point) {
        return new IntPoint2D((int) point.getX(), (int) point.getY());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof IntPoint2D)) {
            return false;
        } else {
            IntPoint2D other = (IntPoint2D) obj;
            return this.getX() == other.getX() && this.getY() == other.getY();
        }
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            long var1 = 7L;
            var1 = 31L * var1 + Integer.toUnsignedLong(getX());
            var1 = 31L * var1 + Double.doubleToLongBits(getY());
            hash = (int) (var1 ^ var1 >> 32);
        }
        return hash;
    }

    @Override
    public String toString() {
        return "IntPoint2D [x = " + getX() + ", y = " + getY() + "]";
    }
}
