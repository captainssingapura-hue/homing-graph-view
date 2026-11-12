package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Scalar;

/**
 * How each layer's flat block is set in the 3D space. Either way the slates face the camera and the block keeps the
 * arrangement the layout gave it: only the plane it lies in, and the way the layers stack, differ.
 */
public sealed interface Stance extends Scalar {

    /**
     * Standing up like a monitor: lanes across, rows up from the floor, and the layers one behind another, the root's
     * in front and the base's at the back.
     */
    record Standing() implements Stance { }

    /**
     * Lying down like the floors of a building: lanes across, rows going back from the front, and the layers one above
     * another, the base's at the bottom and the root's at the top.
     */
    record Lying() implements Stance { }
}
