package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Scalar;

/**
 * How fast a free camera moves, in its own terms: ahead, rightward and upward, in scene units per second. It eases
 * toward what the keys ask for, so movement glides in and out instead of starting and stopping dead.
 *
 * @param ahead     along the view, or the heading for a drone
 * @param rightward sideways
 * @param upward    straight up
 */
public record Velocity(double ahead, double rightward, double upward) implements Scalar {

    /** How quickly speed follows the keys: about 95% of the way in 0.3 seconds. */
    private static final double RESPONSE = 10;
    private static final double STILL = 0.5;

    /**
     * The velocity after some time easing toward another. Exponential, so the result is the same however the time is
     * split into frames.
     */
    public Velocity toward(final Velocity target, final double seconds) {
        final double keep = Math.exp(-RESPONSE * seconds);
        return new Velocity(target.ahead + (ahead - target.ahead) * keep,
                target.rightward + (rightward - target.rightward) * keep,
                target.upward + (upward - target.upward) * keep);
    }

    /** Whether it is too slow to be worth moving. */
    public boolean isStill() {
        return Math.abs(ahead) + Math.abs(rightward) + Math.abs(upward) < STILL;
    }
}
