package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.Scalar;
import java.util.Objects;

/**
 * Where a vertex is laid out: its layer, its lane across the layer and its row up the lane. A renderer turns these into
 * coordinates; nothing here is geometry.
 *
 * @param vertex the vertex
 * @param depth  its layer, 0 for a vertex that depends on nothing
 * @param lane   its lane, centred on 0: lanes of one layer are 1 apart
 * @param row    its row within its lane, from 0
 */
public record Placed(Vertex vertex, int depth, double lane, int row) implements Scalar {

    /** Refuses a negative depth or row, and a lane that is not a number. */
    public Placed {
        Objects.requireNonNull(vertex, "A placed vertex needs its vertex");
        if (depth < 0 || row < 0) {
            throw new IllegalArgumentException("Vertex " + vertex.id() + " is placed at depth " + depth + ", row "
                    + row + "; neither can be negative");
        }
        if (!Double.isFinite(lane)) {
            throw new IllegalArgumentException("Vertex " + vertex.id() + " is placed in lane " + lane);
        }
    }
}
