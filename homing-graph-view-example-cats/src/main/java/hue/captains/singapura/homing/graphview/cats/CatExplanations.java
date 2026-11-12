package hue.captains.singapura.homing.graphview.cats;

import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.viewer.Explanation;
import hue.captains.singapura.homing.graphview.viewer.Explanations;
import java.util.ArrayList;
import java.util.List;

/**
 * What the viewer says of a breed: who it is and its coat, from the studbook, then what it was bred from - or that it
 * is a founder - and what was bred from it, read from the graph.
 */
record CatExplanations() implements Explanations {

    @Override
    public Explanation of(final Vertex vertex, final Graph graph) {
        final List<String> lines = new ArrayList<>();
        new Studbook().breed(vertex.id()).ifPresent(breed -> {
            lines.add(breed.description());
            lines.add("Coat: " + coat(breed.coat()) + ".");
        });
        lines.add("");
        final List<String> parents = graph.edges().items().stream().filter(edge -> edge.from().equals(vertex))
                .map(Edge::to).map(Vertex::id).toList();
        final List<String> children = graph.edges().items().stream().filter(edge -> edge.to().equals(vertex))
                .map(Edge::from).map(Vertex::id).toList();
        lines.add(parents.isEmpty() ? "A founder: bred from no other breed here." : "Bred from: " + and(parents) + ".");
        if (!children.isEmpty()) {
            lines.add("Gave rise to: " + and(children) + ".");
        }
        return new Explanation(vertex.id(), String.join("\n", lines));
    }

    private String coat(final Coat coat) {
        return switch (coat) {
            case Coat.Long() -> "long-haired";
            case Coat.Short() -> "short-haired";
            case Coat.Hairless() -> "hairless";
            case Coat.Wild() -> "a wild cat's";
        };
    }

    /** The names as a list in words: A; A and B; A, B and C. */
    private String and(final List<String> names) {
        return names.size() == 1 ? names.getFirst()
                : String.join(", ", names.subList(0, names.size() - 1)) + " and " + names.getLast();
    }
}
