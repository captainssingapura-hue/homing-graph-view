package hue.captains.singapura.homing.graphview.viewer;

import hue.captains.singapura.homing.graphview.fx.Container;
import hue.captains.singapura.homing.graphview.fx.ContainerShapes;
import hue.captains.singapura.homing.graphview.fx.EdgeStyler;
import hue.captains.singapura.homing.graphview.fx.SameStyle;
import hue.captains.singapura.homing.graphview.fx.Shapes;
import hue.captains.singapura.homing.graphview.fx.Uniform;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.tao.ontology.Scalar;
import java.util.Objects;

/**
 * A graph for the viewer to show, with the functional objects it needs: the shapes its vertices are drawn as, the
 * explanations the details panel gives of them, and the styler its edges are drawn by, made for this graph.
 *
 * @param name         what the viewer's list calls it, unique in its registry
 * @param graph        the graph
 * @param shapes       what each vertex is drawn as
 * @param explanations what the details panel says of each vertex selected
 * @param styler       how each edge of the graph is drawn
 */
public record Exhibit(String name, Graph graph, Shapes shapes, Explanations explanations, EdgeStyler styler)
        implements Scalar {

    /** Refuses a blank name and a missing part. */
    public Exhibit {
        Objects.requireNonNull(name, "An exhibit needs its name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("An exhibit's name cannot be blank");
        }
        Objects.requireNonNull(graph, "Exhibit " + name + " needs its graph");
        Objects.requireNonNull(shapes, "Exhibit " + name + " needs its shapes");
        Objects.requireNonNull(explanations, "Exhibit " + name + " needs its explanations");
        Objects.requireNonNull(styler, "Exhibit " + name + " needs its edges' styler");
    }

    /** A graph with its shapes and explanations, each edge a plain grey rod. */
    public Exhibit(final String name, final Graph graph, final Shapes shapes, final Explanations explanations) {
        this(name, graph, shapes, explanations, new SameStyle());
    }

    /** A graph shown plainly: every vertex in a full container, explained by its name alone, each edge a plain rod. */
    public Exhibit(final String name, final Graph graph) {
        this(name, graph, new ContainerShapes(new Uniform(new Container.Full())), new Names());
    }

    /**
     * This exhibit with its edges drawn by another styler.
     *
     * @param other made for this exhibit's graph
     */
    public Exhibit with(final EdgeStyler other) {
        return new Exhibit(name, graph, shapes, explanations, other);
    }
}
