package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Scalar;

/**
 * Which end of the stack level 0, the base's layer, goes at, the root's going at the other. The ends are named from the
 * starting camera: the far end is the back when the layers stand and the bottom when they lie; the near end is the
 * front when standing and the top when lying.
 */
public sealed interface Order extends Scalar {

    /** Level 0 at the far end: at the back, standing; at the bottom, lying. */
    record BaseFar() implements Order { }

    /** Level 0 at the near end: in front, standing; on top, lying. */
    record BaseNear() implements Order { }
}
