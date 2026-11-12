package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Scalar;
import javafx.geometry.Point3D;

/**
 * A free camera, as a spectator flies: a position, a heading (yaw) and a tilt (pitch), turned in place rather than
 * around a point. The same angles and axes as Orbit, so the two turn into each other exactly. The yaw is kept within
 * [-180, 180) and the pitch short of straight up or down.
 *
 * @param x     where the camera is, x
 * @param y     where it is, y, which points down
 * @param z     where it is, z
 * @param yaw   degrees about the vertical
 * @param pitch degrees above the horizontal; negative looks down
 */
public record FlyCamera(double x, double y, double z, double yaw, double pitch) implements Scalar {

    /** Wraps the yaw and stops the pitch short of the poles, as an orbit does. */
    public FlyCamera {
        final Orbit same = new Orbit(0, 0, 0, yaw, pitch, 1);
        yaw = same.yaw();
        pitch = same.pitch();
    }

    /** Where the camera is. */
    public Point3D position() {
        return new Point3D(x, y, z);
    }

    /** The way the camera looks, of length 1. */
    public Point3D forward() {
        return axes().forward();
    }

    /** Right on the screen, level whatever the tilt. */
    public Point3D right() {
        return axes().right();
    }

    /** The way ahead along the ground: where the camera faces, its tilt left out. */
    public Point3D heading() {
        final double yawRad = Math.toRadians(yaw);
        return new Point3D(Math.sin(yawRad), 0, Math.cos(yawRad));
    }

    /** An orbit around the point this far straight ahead: the same view. */
    public Orbit orbiting(final double distance) {
        final Point3D target = position().add(forward().multiply(distance));
        return new Orbit(target.getX(), target.getY(), target.getZ(), yaw, pitch, distance);
    }

    /** Turned in place: right and up for positive amounts, in degrees. */
    public FlyCamera looked(final double right, final double up) {
        return new FlyCamera(x, y, z, yaw + right, pitch + up);
    }

    /** Moved as a spectator moves: ahead along the view, up or down included; sideways; and straight up. */
    public FlyCamera moved(final double ahead, final double rightward, final double upward) {
        return by(forward().multiply(ahead).add(right().multiply(rightward)).add(up().multiply(upward)));
    }

    /** Moved as a drone moves: ahead along the heading and sideways, both level whatever the tilt, and straight up. */
    public FlyCamera cruised(final double ahead, final double rightward, final double upward) {
        return by(heading().multiply(ahead).add(right().multiply(rightward)).add(up().multiply(upward)));
    }

    /** Kept inside a box, given by two opposite corners. */
    public FlyCamera within(final Point3D low, final Point3D high) {
        return new FlyCamera(Math.max(low.getX(), Math.min(high.getX(), x)),
                Math.max(low.getY(), Math.min(high.getY(), y)),
                Math.max(low.getZ(), Math.min(high.getZ(), z)), yaw, pitch);
    }

    private FlyCamera by(final Point3D step) {
        return new FlyCamera(x + step.getX(), y + step.getY(), z + step.getZ(), yaw, pitch);
    }

    /** Straight up: JavaFX's y points down. */
    private Point3D up() {
        return new Point3D(0, -1, 0);
    }

    private Orbit axes() {
        return new Orbit(x, y, z, yaw, pitch, 1);
    }
}
