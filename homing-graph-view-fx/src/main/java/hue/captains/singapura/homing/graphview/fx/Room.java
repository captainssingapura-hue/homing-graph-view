package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Scalar;

/**
 * The room every figure of one kind fits in, in scene units: its width across, its height up and its depth back. The
 * layout gives each vertex a slot this big and a little more, whatever figure is drawn in it.
 *
 * @param width  across, along x
 * @param height up, along y
 * @param depth  back, along z
 */
public record Room(double width, double height, double depth) implements Scalar {

    /** Refuses a room without size each way. */
    public Room {
        if (!(width > 0 && height > 0 && depth > 0)
                || !(Double.isFinite(width) && Double.isFinite(height) && Double.isFinite(depth))) {
            throw new IllegalArgumentException("A room needs a size each way, not " + width + " by " + height + " by "
                    + depth);
        }
    }
}
