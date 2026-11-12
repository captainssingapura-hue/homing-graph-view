package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Scalar;

/**
 * How a rod is drawn: idle with nothing selected, and otherwise lit, marked or dim. A sealed set of values rather than
 * an enum, whose generated static methods jOntology does not allow a value.
 */
public sealed interface EdgeState extends Scalar {

    /** Nothing is selected. */
    record Idle() implements EdgeState { }

    /** Between two members of the selection. */
    record Lit() implements EdgeState { }

    /** From a member to a marked vertex. */
    record Marked() implements EdgeState { }

    /** Outside the selection. */
    record Dim() implements EdgeState { }
}
