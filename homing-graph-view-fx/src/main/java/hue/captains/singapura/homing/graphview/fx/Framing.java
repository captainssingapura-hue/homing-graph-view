package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import javafx.geometry.Point3D;
import java.util.List;

/**
 * An orbit that shows a set of points from any angle: it looks at their middle from the angle given, and backs off just
 * far enough for every point, with room around it, to fall inside the view.
 */
public record Framing() implements StatelessFunctionalObject {

    private static final double SLACK = 1.06;

    /**
     * The orbit at the angle given that centres the points and shows them all, with room around each.
     *
     * @param points      the points to show
     * @param room        how far around each point must show too, such as half a slate's width
     * @param yaw         the camera's yaw, in degrees
     * @param pitch       its pitch, in degrees
     * @param fieldOfView the camera's vertical field of view, in degrees
     * @param aspect      the view's width over its height
     */
    public Orbit of(final List<Point3D> points, final double room, final double yaw, final double pitch,
                    final double fieldOfView, final double aspect) {
        final Point3D centre = centre(points);
        final Orbit facing = new Orbit(centre.getX(), centre.getY(), centre.getZ(), yaw, pitch, 1);
        final double high = Math.tan(Math.toRadians(fieldOfView / 2));
        final double wide = high * aspect;
        double distance = room;
        for (final Point3D point : points) {
            final Point3D offset = point.subtract(centre);
            final double ahead = offset.dotProduct(facing.forward());
            distance = Math.max(distance, (Math.abs(offset.dotProduct(facing.right())) + room) / wide - ahead);
            distance = Math.max(distance, (Math.abs(offset.dotProduct(facing.down())) + room) / high - ahead);
        }
        return new Orbit(centre.getX(), centre.getY(), centre.getZ(), yaw, pitch, Math.max(distance, 1) * SLACK);
    }

    /** The middle of the points' bounding box; the origin when there are none. */
    private Point3D centre(final List<Point3D> points) {
        if (points.isEmpty()) {
            return Point3D.ZERO;
        }
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (final Point3D point : points) {
            minX = Math.min(minX, point.getX());
            minY = Math.min(minY, point.getY());
            minZ = Math.min(minZ, point.getZ());
            maxX = Math.max(maxX, point.getX());
            maxY = Math.max(maxY, point.getY());
            maxZ = Math.max(maxZ, point.getZ());
        }
        return new Point3D((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
    }
}
