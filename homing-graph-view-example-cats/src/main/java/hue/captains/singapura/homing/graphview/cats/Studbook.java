package hue.captains.singapura.homing.graphview.cats;

import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Edges;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.model.Vertices;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The cat breeds, read from {@code cat-breeds.txt} beside this class, a line each: name, coat, the breeds it was bred
 * from by commas, and who it is, split by bars. As a graph, each breed is a vertex with an edge to each breed it was
 * bred from, so the founders, bred from none, are level 0.
 */
record Studbook() implements StatelessFunctionalObject {

    /** The breeds as a graph, in the studbook's order. */
    Graph graph() {
        final Map<String, Vertex> breeds = new LinkedHashMap<>();
        rows().forEach(row -> breeds.put(row[0], new Vertex(row[0])));
        final List<Edge> edges = new ArrayList<>();
        for (final String[] row : rows()) {
            for (final String parent : parents(row)) {
                final Vertex from = Objects.requireNonNull(breeds.get(parent),
                        row[0] + " was bred from " + parent + ", which the studbook does not have");
                edges.add(new Edge(breeds.get(row[0]), from));
            }
        }
        return new Graph(new Vertices(List.copyOf(breeds.values())), new Edges(edges));
    }

    /**
     * A breed, or nothing for a name the studbook does not have.
     *
     * @param name the breed's name
     */
    Optional<Breed> breed(final String name) {
        return rows().stream().filter(row -> row[0].equals(name)).findFirst()
                .map(row -> new Breed(row[0], coat(row[1]), row[3]));
    }

    private List<String> parents(final String[] row) {
        return Arrays.stream(row[2].split(",")).map(String::strip).filter(parent -> !parent.isEmpty()).toList();
    }

    private Coat coat(final String word) {
        return switch (word) {
            case "long" -> new Coat.Long();
            case "short" -> new Coat.Short();
            case "hairless" -> new Coat.Hairless();
            case "wild" -> new Coat.Wild();
            default -> throw new IllegalArgumentException("The studbook has no coat called " + word);
        };
    }

    /** The studbook's lines, notes left out, each split into its four fields. */
    private List<String[]> rows() {
        try (InputStream stream = Objects.requireNonNull(Studbook.class.getResourceAsStream("cat-breeds.txt"),
                "The example is missing cat-breeds.txt");
             BufferedReader lines = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return lines.lines().filter(line -> !line.isBlank() && !line.startsWith("#"))
                    .map(line -> Arrays.stream(line.split("\\|", -1)).map(String::strip).toArray(String[]::new))
                    .toList();
        } catch (final IOException unread) {
            throw new UncheckedIOException("Cannot read cat-breeds.txt", unread);
        }
    }
}
