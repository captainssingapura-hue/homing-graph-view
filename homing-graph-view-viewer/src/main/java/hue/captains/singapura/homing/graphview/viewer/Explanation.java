package hue.captains.singapura.homing.graphview.viewer;

import hue.captains.singapura.tao.ontology.Scalar;
import java.util.Objects;

/**
 * What the viewer's details panel says of a vertex selected: a heading and, under it, as much more as there is to say.
 *
 * @param heading what to call the vertex
 * @param body    what more there is to say of it, or nothing
 */
public record Explanation(String heading, String body) implements Scalar {

    /** Refuses a missing heading or body. */
    public Explanation {
        Objects.requireNonNull(heading, "An explanation needs its heading");
        Objects.requireNonNull(body, "An explanation needs its body, even an empty one");
    }
}
