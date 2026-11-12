package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.Scalar;
import java.util.Objects;

/**
 * Where a vertex sits in its layer's block.
 *
 * @param vertex the vertex
 * @param slot   its slot: its lane across the block, centred on 0, and its row up it, 0 at the bottom
 */
public record Seat(Vertex vertex, Slot slot) implements Scalar {

    /** Refuses a seat without its vertex or its slot. */
    public Seat {
        Objects.requireNonNull(vertex, "A seat needs its vertex");
        Objects.requireNonNull(slot, "A seat needs its slot");
    }
}
