package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Scalar;
import javafx.geometry.Point3D;
import java.util.List;

/**
 * The ground the graph stands on and the air above it: a floor a little below the lowest slate, reaching well beyond
 * the graph on every side, and the box a free camera may roam in, from just above the floor to well above the graph.
 *
 * @param minX    the floor's left edge
 * @param maxX    its right edge
 * @param minZ    its front edge
 * @param maxZ    its back edge
 * @param floor   its height; y points down, so the floor's y is the largest
 * @param frontZ  the front of the graph itself, where a walk in starts from
 */
public record Grounds(double minX, double maxX, double minZ, double maxZ, double floor, double frontZ)
        implements Scalar {

    private static final double BEYOND = 1600;
    private static final double BELOW = 70;
    private static final double ROAM_BEYOND = 2500;
    private static final double CEILING = 6000;
    private static final double LOWEST = 40;
    private static final double EYE_HEIGHT = 150;
    private static final double STAND_BACK = 1100;
    private static final double LOOK_UP = 6;

    /**
     * The grounds under slates centred at the points.
     *
     * @param centres    the slates' centres
     * @param halfHeight half a slate's height
     */
    public Grounds(final List<Point3D> centres, final double halfHeight) {
        this(centres.stream().mapToDouble(Point3D::getX).min().orElse(0) - BEYOND,
                centres.stream().mapToDouble(Point3D::getX).max().orElse(0) + BEYOND,
                centres.stream().mapToDouble(Point3D::getZ).min().orElse(0) - BEYOND,
                centres.stream().mapToDouble(Point3D::getZ).max().orElse(0) + BEYOND,
                centres.stream().mapToDouble(Point3D::getY).max().orElse(0) + halfHeight + BELOW,
                centres.stream().mapToDouble(Point3D::getZ).min().orElse(0));
    }

    /** The low corner of the box a free camera may roam in. */
    public Point3D roamLow() {
        return new Point3D(minX - ROAM_BEYOND, floor - CEILING, minZ - ROAM_BEYOND);
    }

    /** Its high corner: just above the floor. */
    public Point3D roamHigh() {
        return new Point3D(maxX + ROAM_BEYOND, floor - LOWEST, maxZ + ROAM_BEYOND);
    }

    /** How high a point is above the floor. */
    public double altitude(final Point3D point) {
        return floor - point.getY();
    }

    /** Standing on the floor in front of the graph, looking in. */
    public FlyCamera entrance() {
        return new FlyCamera((minX + maxX) / 2, floor - EYE_HEIGHT, frontZ - STAND_BACK, 0, LOOK_UP);
    }
}
