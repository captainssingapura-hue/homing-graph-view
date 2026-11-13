package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Scalar;

/**
 * What an edge is along its length, one of a few: a solid rod, dashes, beads, or two rods side by side. Dashes and
 * beads are sized by the edge's girth.
 */
public sealed interface Texture extends Scalar {

    /** A solid rod. */
    record Solid() implements Texture { }

    /** A rod broken into dashes. */
    record Dashed() implements Texture { }

    /** A line of beads. */
    record Dotted() implements Texture { }

    /** Two thinner rods side by side, as a family tree draws a marriage. */
    record Twin() implements Texture { }
}
