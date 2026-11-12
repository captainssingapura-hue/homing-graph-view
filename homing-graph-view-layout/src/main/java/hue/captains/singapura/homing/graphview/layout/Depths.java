package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Edges;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Each vertex's depth: the longest path from it to a vertex with no edges out, so 0 for a vertex that depends on
 * nothing and one more than its deepest dependency otherwise. Vertices on a cycle depend on each other, so they share a
 * depth: each strongly connected component is taken as one vertex, and the components are laid out instead.
 */
public record Depths() implements StatelessFunctionalObject {

    /**
     * The depth of every vertex of the graph.
     *
     * @param graph the graph, cycles allowed
     */
    public Map<Vertex, Integer> of(final Graph graph) {
        final Map<Vertex, Integer> component = components(graph, finishOrder(graph));
        final int[] depth = new int[component.size()];
        final List<Edge> edges = new ArrayList<>(graph.edges().items());
        edges.sort(Comparator.comparing((Edge edge) -> component.get(edge.from())).reversed());
        for (final Edge edge : edges) {
            final int from = component.get(edge.from());
            final int to = component.get(edge.to());
            if (from != to) {
                depth[from] = Math.max(depth[from], depth[to] + 1);
            }
        }
        final Map<Vertex, Integer> depths = new HashMap<>();
        component.forEach((vertex, number) -> depths.put(vertex, depth[number]));
        return Map.copyOf(depths);
    }

    /** The vertices in the order a depth-first walk along the edges finishes with them. */
    private List<Vertex> finishOrder(final Graph graph) {
        final List<Vertex> finished = new ArrayList<>();
        final Set<Vertex> seen = new HashSet<>();
        for (final Vertex start : graph.vertices().items()) {
            if (seen.add(start)) {
                walk(graph.edges(), start, seen, finished);
            }
        }
        return finished;
    }

    private void walk(final Edges edges, final Vertex start, final Set<Vertex> seen, final List<Vertex> finished) {
        final Deque<Vertex> path = new ArrayDeque<>(List.of(start));
        final Deque<Iterator<Edge>> pending = new ArrayDeque<>();
        pending.push(edges.from(start).items().iterator());
        while (!pending.isEmpty()) {
            final Iterator<Edge> next = pending.peek();
            if (next.hasNext()) {
                final Vertex to = next.next().to();
                if (seen.add(to)) {
                    path.push(to);
                    pending.push(edges.from(to).items().iterator());
                }
            } else {
                pending.pop();
                finished.add(path.pop());
            }
        }
    }

    /**
     * Each vertex's strongly connected component, found by walking the edges backwards in reverse finishing order
     * (Kosaraju). The components come out sources first, so an edge between two goes from a lower number to a higher.
     */
    private Map<Vertex, Integer> components(final Graph graph, final List<Vertex> finished) {
        final Map<Vertex, Integer> component = new HashMap<>();
        int count = 0;
        for (int i = finished.size() - 1; i >= 0; i--) {
            final Vertex start = finished.get(i);
            if (component.putIfAbsent(start, count) != null) {
                continue;
            }
            final Deque<Vertex> pending = new ArrayDeque<>(List.of(start));
            while (!pending.isEmpty()) {
                for (final Edge edge : graph.edges().to(pending.pop()).items()) {
                    if (component.putIfAbsent(edge.from(), count) == null) {
                        pending.push(edge.from());
                    }
                }
            }
            count++;
        }
        return component;
    }
}
