package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.homing.graphview.model.Edges;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.model.Vertices;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import java.util.List;

/**
 * The default focus: a vertex uses everything it reaches. Members are the closure, nothing is marked, and every edge
 * inside the closure is lit - right for a Maven tree or a module graph.
 */
public record Reachability() implements Focus, StatelessFunctionalObject {

    @Override
    public Highlight of(final Graph graph, final Vertex root) {
        final Vertices members = new Closure().of(graph, root);
        final Edges lit = new Edges(graph.edges().items().stream()
                .filter(edge -> members.contains(edge.from()) && members.contains(edge.to()))
                .toList());
        return new Highlight(root, members, new Vertices(List.of()), lit, new Edges(List.of()));
    }
}
