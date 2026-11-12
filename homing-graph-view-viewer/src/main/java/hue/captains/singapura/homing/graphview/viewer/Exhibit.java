package hue.captains.singapura.homing.graphview.viewer;

import hue.captains.singapura.homing.graphview.fx.Container;
import hue.captains.singapura.homing.graphview.fx.ContainerShapes;
import hue.captains.singapura.homing.graphview.fx.Shapes;
import hue.captains.singapura.homing.graphview.fx.Uniform;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.tao.ontology.Scalar;
import java.util.Objects;

/**
 * A graph for the viewer to show, with the functional objects it needs: the shapes its vertices are drawn as, and the
 * explanations the details panel gives of them.
 *
 * @param name         what the viewer's list calls it, unique in its registry
 * @param graph        the graph
 * @param shapes       what each vertex is drawn as
 * @param explanations what the details panel says of each vertex selected
 */
public record Exhibit(String name, Graph graph, Shapes shapes, Explanations explanations) implements Scalar {

    /** Refuses a blank name and a missing part. */
    public Exhibit {
        Objects.requireNonNull(name, "An exhibit needs its name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("An exhibit's name cannot be blank");
        }
        Objects.requireNonNull(graph, "Exhibit " + name + " needs its graph");
        Objects.requireNonNull(shapes, "Exhibit " + name + " needs its shapes");
        Objects.requireNonNull(explanations, "Exhibit " + name + " needs its explanations");
    }

    /** A graph shown plainly: every vertex in a full container, explained by its name alone. */
    public Exhibit(final String name, final Graph graph) {
        this(name, graph, new ContainerShapes(new Uniform(new Container.Full())), new Names());
    }
}
