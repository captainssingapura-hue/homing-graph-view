package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Scalar;
import javafx.geometry.Point3D;

/**
 * Where the camera is: looking at a target from a distance, turned by a yaw about the vertical and a pitch above or
 * below the horizontal, both in degrees. JavaFX's axes: x to the right, y down and z away from a camera at yaw 0, so a
 * negative pitch looks down on the target. The yaw is kept within [-180, 180) and the pitch short of straight up or down.
 *
 * @param x        the target's x
 * @param y        the target's y
 * @param z        the target's z
 * @param yaw      degrees about the vertical
 * @param pitch    degrees above the horizontal; negative looks down
 * @param distance from the camera to the target, positive
 */
public record Orbit(double x, double y, double z, double yaw, double pitch, double distance) implements Scalar {

    private static final double STEEPEST = 89;

    /** Refuses a distance that is not positive; wraps the yaw and stops the pitch short of the poles. */
    public Orbit {
        if (!(distance > 0) || Double.isInfinite(distance)) {
            throw new IllegalArgumentException("An orbit's distance must be positive, not " + distance);
        }
        yaw = ((yaw % 360) + 540) % 360 - 180;
        pitch = Math.max(-STEEPEST, Math.min(STEEPEST, pitch));
    }

    /** The point looked at. */
    public Point3D target() {
        return new Point3D(x, y, z);
    }

    /** Where the camera is. */
    public Point3D eye() {
        return target().subtract(forward().multiply(distance));
    }

    /** The way the camera looks, of length 1. */
    public Point3D forward() {
        final double yawRad = Math.toRadians(yaw);
        final double pitchRad = Math.toRadians(pitch);
        return new Point3D(Math.cos(pitchRad) * Math.sin(yawRad), -Math.sin(pitchRad),
                Math.cos(pitchRad) * Math.cos(yawRad));
    }

    /** Right on the screen, in the scene, of length 1. */
    public Point3D right() {
        final double yawRad = Math.toRadians(yaw);
        return new Point3D(Math.cos(yawRad), 0, -Math.sin(yawRad));
    }

    /** Down the screen, in the scene, of length 1. */
    public Point3D down() {
        final double yawRad = Math.toRadians(yaw);
        final double pitchRad = Math.toRadians(pitch);
        return new Point3D(Math.sin(pitchRad) * Math.sin(yawRad), Math.cos(pitchRad),
                Math.sin(pitchRad) * Math.cos(yawRad));
    }

    /** The same view from a free camera, standing where this one is and looking the same way. */
    public FlyCamera freed() {
        final Point3D eye = eye();
        return new FlyCamera(eye.getX(), eye.getY(), eye.getZ(), yaw, pitch);
    }

    /** Turned about the target. */
    public Orbit turned(final double byYaw, final double byPitch) {
        return new Orbit(x, y, z, yaw + byYaw, pitch + byPitch, distance);
    }

    /**
     * Panned by a drag across the screen: the target moves against the drag, so the scene follows the cursor.
     *
     * @param right how far the drag went right, in scene units at the target
     * @param down  how far it went down, in the same units
     */
    public Orbit panned(final double right, final double down) {
        return at(target().subtract(right().multiply(right)).subtract(down().multiply(down)), distance);
    }

    /**
     * Zoomed toward a point on the screen, which stays where it is: closer for a factor below 1, further above.
     *
     * @param factor the new distance over the old
     * @param right  how far right of the target the point is on the screen, in scene units at the target
     * @param down   how far below it, in the same units
     */
    public Orbit zoomed(final double factor, final double right, final double down) {
        final Point3D toward = right().multiply(right).add(down().multiply(down));
        return at(target().add(toward.multiply(1 - factor)), distance * factor);
    }

    /** Part of the way to another orbit, from 0 (this one) to 1 (that one), turning the short way round. */
    public Orbit toward(final Orbit other, final double share) {
        final double turn = ((other.yaw - yaw) % 360 + 540) % 360 - 180;
        return new Orbit(x + (other.x - x) * share, y + (other.y - y) * share, z + (other.z - z) * share,
                yaw + turn * share, pitch + (other.pitch - pitch) * share,
                distance * Math.pow(other.distance / distance, share));
    }

    private Orbit at(final Point3D target, final double distance) {
        return new Orbit(target.getX(), target.getY(), target.getZ(), yaw, pitch, distance);
    }
}
