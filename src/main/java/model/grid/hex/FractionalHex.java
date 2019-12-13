package model.grid.hex;

import javafx.geometry.Point2D;

public class FractionalHex {

    private final double m_q, m_r, m_s;

    public FractionalHex(double x, double z, double y) {
        if (x + z + y != 0) {
            throw new IllegalArgumentException(
                "Constraint q+r+s=0 must be satisfied."
            );
        }
        m_q = x;
        m_r = z;
        m_s = y;
    }

    public FractionalHex(double x, double z) {
        this(x, z, -x - z);
    }

    public double q() {
        return m_q;
    }

    public double r() {
        return m_r;
    }

    public double s() {
        return m_s;
    }

    public static Point2D round(Point2D axial) {
        return round(axial.getX(), axial.getY());
    }

    public static Point2D round(double m_q, double m_r) {
        double m_s = -m_q - m_r;
        int q = (int) Math.round(m_q);
        int r = (int) Math.round(m_r);
        int s = (int) Math.round(m_s);
        double q_diff = Math.abs(q - m_q);
        double r_diff = Math.abs(r - m_r);
        double s_diff = Math.abs(s - m_s);
        if (q_diff > r_diff && q_diff > s_diff) {
            q = -r - s;
        } else if (r_diff > s_diff) {
            r = -q - s;
        }
        return new Point2D(q, r);
    }

    public Hex round() {
        Point2D roundedAxial = round(m_q, m_r);
        int rounded_q = (int) roundedAxial.getX();
        int rounded_r = (int) roundedAxial.getY();
        return new Hex(rounded_q, rounded_r, -rounded_q - rounded_r);
    }
}
