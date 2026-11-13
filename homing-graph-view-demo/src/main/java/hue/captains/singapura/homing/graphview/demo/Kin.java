package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A person's family as the royal family's graph shows it: a child points to each parent, and a marriage is a pair of
 * edges, one each way. Their parents, whom they married and their children, a line each, in the graph's order; a line
 * with no one in it is left out.
 */
record Kin() implements StatelessFunctionalObject {

    /**
     * The person's family, in words.
     *
     * @param graph  the family's graph
     * @param person someone in it
     */
    String of(final Graph graph, final Vertex person) {
        final Set<Edge> edges = Set.copyOf(graph.edges().items());
        final List<Vertex> others = graph.vertices().items().stream().filter(other -> !other.equals(person)).toList();
        final List<String> lines = new ArrayList<>();
        line(lines, "Parent", "Parents", others, other -> edges.contains(new Edge(person, other))
                && !edges.contains(new Edge(other, person)));
        line(lines, "Married to", "Married to", others, other -> edges.contains(new Edge(person, other))
                && edges.contains(new Edge(other, person)));
        line(lines, "Child", "Children", others, other -> edges.contains(new Edge(other, person))
                && !edges.contains(new Edge(person, other)));
        return String.join("\n", lines);
    }

    /** A line naming everyone else who is kin this way, if anyone is. */
    private void line(final List<String> lines, final String one, final String many, final List<Vertex> others,
                      final Predicate<Vertex> kin) {
        final List<String> names = others.stream().filter(kin).map(Vertex::id).toList();
        if (!names.isEmpty()) {
            lines.add((names.size() == 1 ? one : many) + ": " + and(names));
        }
    }

    /** The names as a list in words: A; A and B; A, B and C. */
    private String and(final List<String> names) {
        return names.size() == 1 ? names.getFirst()
                : String.join(", ", names.subList(0, names.size() - 1)) + " and " + names.getLast();
    }
}
