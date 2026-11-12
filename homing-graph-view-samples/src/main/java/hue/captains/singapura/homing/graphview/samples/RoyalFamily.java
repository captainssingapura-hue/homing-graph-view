package hue.captains.singapura.homing.graphview.samples;

import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Edges;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.model.Vertices;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The British Royal Family from Queen Elizabeth II and Prince Philip down to their great-grandchildren, as a graph: a
 * vertex for each person, named, and an edge from each child to each parent shown. A marriage is a pair of edges, one
 * each way, so the couple are on a cycle and share a layer: someone who married in sits with their spouse's generation.
 * The layers are then the generations, Elizabeth and Philip's at depth 0.
 */
public record RoyalFamily() implements StatelessFunctionalObject {

    /** The family, generation by generation and family by family. */
    public Graph of() {
        final Map<String, Vertex> people = new LinkedHashMap<>();
        final List<Edge> edges = new ArrayList<>();
        for (final List<String> marriage : marriages()) {
            final Vertex one = person(people, marriage.get(0));
            final Vertex other = person(people, marriage.get(1));
            edges.add(new Edge(one, other));
            edges.add(new Edge(other, one));
        }
        for (final List<String> family : children()) {
            final Vertex child = person(people, family.getFirst());
            family.subList(1, family.size()).forEach(parent -> edges.add(new Edge(child, people.get(parent))));
        }
        return new Graph(new Vertices(List.copyOf(people.values())), new Edges(edges));
    }

    private Vertex person(final Map<String, Vertex> people, final String name) {
        return people.computeIfAbsent(name, Vertex::new);
    }

    /** The marriages shown, the couples' elder generation first. */
    private List<List<String>> marriages() {
        return List.of(
                List.of("Queen Elizabeth II", "Prince Philip"),
                List.of("King Charles III", "Diana Spencer"),
                List.of("King Charles III", "Queen Camilla"));
    }

    /** Each child and its parents shown, generation by generation. */
    private List<List<String>> children() {
        return List.of(
                List.of("Princess Anne", "Queen Elizabeth II", "Prince Philip"),
                List.of("Andrew Mountbatten-Windsor", "Queen Elizabeth II", "Prince Philip"),
                List.of("Prince Edward", "Queen Elizabeth II", "Prince Philip"),
                List.of("King Charles III", "Queen Elizabeth II", "Prince Philip"),
                List.of("Prince William", "King Charles III", "Diana Spencer"),
                List.of("Prince Harry", "King Charles III", "Diana Spencer"),
                List.of("Peter Phillips", "Princess Anne"),
                List.of("Zara Tindall", "Princess Anne"),
                List.of("Princess Beatrice", "Andrew Mountbatten-Windsor"),
                List.of("Princess Eugenie", "Andrew Mountbatten-Windsor"),
                List.of("Lady Louise", "Prince Edward"),
                List.of("James, Earl of Wessex", "Prince Edward"),
                List.of("Prince George", "Prince William"),
                List.of("Princess Charlotte", "Prince William"),
                List.of("Prince Louis", "Prince William"),
                List.of("Prince Archie", "Prince Harry"),
                List.of("Princess Lilibet", "Prince Harry"),
                List.of("Savannah", "Peter Phillips"),
                List.of("Isla", "Peter Phillips"),
                List.of("Mia", "Zara Tindall"),
                List.of("Lena", "Zara Tindall"),
                List.of("Lucas", "Zara Tindall"),
                List.of("Sienna", "Princess Beatrice"),
                List.of("Athena", "Princess Beatrice"),
                List.of("August", "Princess Eugenie"),
                List.of("Ernest", "Princess Eugenie"),
                List.of("Adelaide", "Princess Eugenie"));
    }
}
