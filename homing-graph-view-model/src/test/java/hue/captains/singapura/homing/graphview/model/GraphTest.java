package hue.captains.singapura.homing.graphview.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    private final Vertex core = new Vertex("core");
    private final Vertex text = new Vertex("text");
    private final Vertex app = new Vertex("app");

    @Test
    void aVertexIsItsId() {
        assertEquals(new Vertex("core"), core);
        assertNotEquals(text, core);
    }

    @Test
    void aBlankIdIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new Vertex(" "));
        assertThrows(IllegalArgumentException.class, () -> new Vertex(""));
        assertThrows(NullPointerException.class, () -> new Vertex(null));
    }

    @Test
    void anEdgeIsItsTwoEndsInOrder() {
        assertEquals(new Edge(text, core), new Edge(text, core));
        assertNotEquals(new Edge(core, text), new Edge(text, core));
    }

    @Test
    void anEdgeFromAVertexToItselfIsRefused() {
        final IllegalArgumentException refused =
                assertThrows(IllegalArgumentException.class, () -> new Edge(core, new Vertex("core")));
        assertTrue(refused.getMessage().contains("core"));
    }

    @Test
    void aVertexGivenTwiceIsRefused() {
        final IllegalArgumentException refused = assertThrows(IllegalArgumentException.class,
                () -> new Vertices(List.of(core, text, new Vertex("core"))));
        assertTrue(refused.getMessage().contains("core"));
    }

    @Test
    void aPairGivenTwiceIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> new Edges(List.of(new Edge(text, core), new Edge(text, core))));
    }

    @Test
    void anEdgeBackIsAnotherEdge() {
        assertEquals(2, new Edges(List.of(new Edge(text, core), new Edge(core, text))).size());
    }

    @Test
    void anEdgeToAVertexOutsideTheGraphIsRefused() {
        final IllegalArgumentException refused = assertThrows(IllegalArgumentException.class,
                () -> new Graph(new Vertices(List.of(core, text)), new Edges(List.of(new Edge(app, core)))));
        assertTrue(refused.getMessage().contains("app"));
    }

    @Test
    void orderIsPartOfTheValue() {
        final Graph graph = graph(List.of(core, text, app));
        assertEquals(graph(List.of(core, text, app)), graph);
        assertEquals(graph(List.of(core, text, app)).hashCode(), graph.hashCode());
        assertNotEquals(graph(List.of(app, text, core)), graph);
        assertEquals(List.of(core, text, app), graph.vertices().items());
    }

    @Test
    void theEdgesFromAndToAVertexAreFoundInOrder() {
        final Graph graph = graph(List.of(core, text, app));
        assertEquals(List.of(new Edge(app, text), new Edge(app, core)), graph.edges().from(app).items());
        assertEquals(List.of(new Edge(text, core), new Edge(app, core)), graph.edges().to(core).items());
        assertEquals(0, graph.edges().from(core).size());
        assertEquals(0, graph.edges().to(app).size());
    }

    @Test
    void aGraphWithNoEdgesIsAGraph() {
        final Graph graph = new Graph(new Vertices(List.of(core)), new Edges(List.of()));
        assertTrue(graph.vertices().contains(core));
        assertFalse(graph.vertices().contains(text));
        assertEquals(0, graph.edges().size());
    }

    @Test
    void aValueDoesNotChangeWithTheListItWasGiven() {
        final List<Vertex> given = new ArrayList<>(List.of(core, text));
        final Vertices vertices = new Vertices(given);
        given.add(app);
        assertEquals(2, vertices.size());
        assertFalse(vertices.contains(app));
        assertThrows(UnsupportedOperationException.class, () -> vertices.items().add(app));
    }

    private Graph graph(final List<Vertex> order) {
        return new Graph(new Vertices(order), new Edges(List.of(
                new Edge(text, core),
                new Edge(app, text),
                new Edge(app, core))));
    }
}
