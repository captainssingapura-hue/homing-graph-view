package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.model.Vertex;
import java.util.Objects;

/**
 * Every vertex in the same container: the default, a full one, when nothing says how big the vertices are.
 *
 * @param container the container every vertex comes in
 */
public record Uniform(Container container) implements Containers {

    /** Refuses a missing container. */
    public Uniform {
        Objects.requireNonNull(container, "Every vertex in the same container needs that container");
    }

    @Override
    public Container of(final Vertex vertex) {
        return container;
    }
}
