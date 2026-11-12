package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.tao.ontology.Scalar;
import java.util.Objects;

/**
 * What the demo's details panel says of a vertex selected.
 *
 * @param heading its name
 * @param body    what more there is to say of it, or nothing
 */
record Detail(String heading, String body) implements Scalar {

    /** Refuses a missing heading or body. */
    Detail {
        Objects.requireNonNull(heading, "A detail needs its heading");
        Objects.requireNonNull(body, "A detail needs its body, even an empty one");
    }
}
