package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Scalar;

/**
 * How a container is drawn: idle with nothing selected, and otherwise one of the four states a selection puts it in. A
 * sealed set of values rather than an enum, whose generated static methods jOntology does not allow a value.
 */
public sealed interface NodeState extends Scalar {

    /** Nothing is selected. */
    record Idle() implements NodeState { }

    /** The vertex selected. */
    record Selected() implements NodeState { }

    /** One of the vertices the selection uses. */
    record Member() implements NodeState { }

    /** Reached by the selection but not used by it. */
    record Marked() implements NodeState { }

    /** Outside the selection. */
    record Dim() implements NodeState { }
}
