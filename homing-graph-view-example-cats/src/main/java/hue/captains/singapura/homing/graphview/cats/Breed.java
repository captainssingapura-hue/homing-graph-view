package hue.captains.singapura.homing.graphview.cats;

import hue.captains.singapura.tao.ontology.Scalar;
import java.util.Objects;

/**
 * A cat breed, as the studbook has it; the breeds it was bred from are the graph's edges.
 *
 * @param name        its name
 * @param coat        its coat
 * @param description who it is, in a line
 */
record Breed(String name, Coat coat, String description) implements Scalar {

    /** Refuses a missing part. */
    Breed {
        Objects.requireNonNull(name, "A breed needs its name");
        Objects.requireNonNull(coat, "Breed " + name + " needs its coat");
        Objects.requireNonNull(description, "Breed " + name + " needs its description");
    }
}
