package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.Scalar;
import java.util.Objects;

/**
 * The event a view emits when its selection changes: some vertex is selected now, or nothing is. A listener can switch
 * on the two and clear whatever it shows for the last vertex as easily as it shows the next.
 */
public sealed interface VertexSelection extends Scalar {

    /**
     * A vertex is selected.
     *
     * @param vertex the vertex selected
     */
    record Some(Vertex vertex) implements VertexSelection {

        /** Refuses a missing vertex. */
        public Some {
            Objects.requireNonNull(vertex, "Some selection needs the vertex selected");
        }
    }

    /** Nothing is selected. */
    record Nothing() implements VertexSelection { }
}
