package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Scalar;

/** What is at the end of an edge where the vertex depended on is: nothing, or an arrowhead, sized by its girth. */
public sealed interface Head extends Scalar {

    /** Nothing: the edge just ends. */
    record None() implements Head { }

    /** An arrowhead, pointing at the vertex depended on. */
    record Arrow() implements Head { }
}
