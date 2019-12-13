package model.grid.hex;

import util.IntPoint2D;

/**
 * Reference: https://www.redblobgames.com/grids/hexagons/implementation.html
 *
 * Implements a hex in a grid with a cube coordinate system interface, but using
 * a axial coordinate system internally.
 */
public class Hex {

    public static final Hex ORIGIN = new Hex(0, 0, 0);
    public static Hex[] DIRECTIONS = {
        new Hex(1, 0), new Hex(1, -1), new Hex(0, -1),
        new Hex(-1, 0), new Hex(-1, 1), new Hex(0, 1)
    };
    public static IntPoint2D[] POINT_DIRECTIONS = {
        new IntPoint2D(1, 0), new IntPoint2D(1, -1),
        new IntPoint2D(0, -1), new IntPoint2D(-1, 0),
        new IntPoint2D(-1, 1), new IntPoint2D(0, 1)
    };

    private final int m_q, m_r, m_s;
    private IntPoint2D pointRepresentation;

    /**
     * Cube system constructor
     *
     * @param x the coordinate on the cube x-axis
     * @param z the coordinate on the cube z-axis
     * @param y the coordinate on the cube y-axis
     */
    public Hex(int x, int z, int y) {
        if (x + z + y != 0) {
            throw new IllegalArgumentException(
                "Constraint q+r+s=0 must be satisfied."
            );
        }
        m_q = x;
        m_r = z;
        m_s = y;
        pointRepresentation = new IntPoint2D(m_q, m_r);
    }

    /**
     * Axial system constructor
     *
     * @param x the coordinate on the cube x-axis
     * @param z the coordinate on the cube z-axis
     */
    public Hex(int x, int z) {
        this(x, z, -x - z);
    }

    public int q() {
        return m_q;
    }

    public int r() {
        return m_r;
    }

    public int s() {
        return m_s;
    }

    public IntPoint2D getPoint() {
        return pointRepresentation;
    }

    public Hex add(Hex other) {
        return new Hex(
            m_q + other.m_q,
            m_r + other.m_r,
            m_s + other.m_s
        );
    }

    public Hex subtract(Hex other) {
        return new Hex(
            m_q - other.m_q,
            m_r - other.m_r,
            m_s - other.m_s
        );
    }

    public Hex multiply(int k) {
        return new Hex(
            m_q * k,
            m_r * k,
            m_s * k
        );
    }

    public int length() {
        return distanceTo(ORIGIN);
    }

    public int distanceTo(Hex hex) {
        return (
            Math.abs(m_q - hex.m_q) +
            Math.abs(m_r - hex.m_r) +
            Math.abs(m_s - hex.m_s)
        ) / 2;
    }

    /**
     * Returns one of the six possible directions
     *
     * @param index direction index in [0, 5]
     * @return direction hex
     */
    public Hex direction(int index) {
        return DIRECTIONS[index];
    }

    public Hex neighbor(int index) {
        return add(direction(index));
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (object.getClass() != Hex.class) {
            return false;
        }
        Hex other = (Hex) object;
        return m_q == other.m_q && m_r == other.m_r && m_s == other.m_s;
    }

    @Override
    public String toString() {
        return "Hex[" + m_q + ", " + m_r + ", " + m_s + "]";
    }
}
