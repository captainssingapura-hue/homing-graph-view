package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Edges;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.model.Vertices;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The closure, the layers by depth and the default focus, over small graphs. */
class LayoutTest {

    private final Vertex app = new Vertex("app");
    private final Vertex web = new Vertex("web");
    private final Vertex text = new Vertex("text");
    private final Vertex core = new Vertex("core");
    private final Vertex json = new Vertex("json");

    /** app needs web and core, web needs text, text needs core; json stands apart. */
    private final Graph estate = graph(List.of(app, web, text, core, json),
            new Edge(app, web), new Edge(app, core), new Edge(web, text), new Edge(text, core));

    /** Each layer in a block of 230 by 80 slots on a 16:9 screen, growing by half again. */
    private final LayersByDepth layering = new LayersByDepth(new Ladder(230.0 / 80, 16.0 / 9, 1.5));

    @Test
    void depthIsOneMoreThanTheDeepestDependency() {
        final Placement placement = layering.of(estate);
        assertEquals(3, depth(placement, app));
        assertEquals(2, depth(placement, web));
        assertEquals(1, depth(placement, text));
        assertEquals(0, depth(placement, core));
        assertEquals(0, depth(placement, json));
        assertEquals(3, placement.maxDepth());
    }

    @Test
    void eachLayerIsArrangedInItsOwnBlockInTheGraphsOrder() {
        final Placement placement = layering.of(estate);
        // core and json share layer 0: a block one slot wide and two high, core on top as it comes first
        assertEquals(List.of(0.0, 0.0), List.of(lane(placement, core), lane(placement, json)));
        assertEquals(List.of(1, 0), List.of(row(placement, core), row(placement, json)));
        assertEquals(List.of(0.0, 0), List.of(lane(placement, app), row(placement, app)));
        assertEquals(List.of(2, 1, 1, 1, 0), List.of(placement.rowsOf(0), placement.rowsOf(1), placement.rowsOf(2),
                placement.rowsOf(3), placement.rowsOf(4)));
    }

    @Test
    void thePlacementKeepsTheGraphsOrder() {
        final Placement placement = layering.of(estate);
        assertEquals(estate.vertices().items(), placement.items().stream().map(Placed::vertex).toList());
    }

    @Test
    void verticesOnACycleShareALayer() {
        final Graph cyclic = graph(List.of(app, web, text, core),
                new Edge(app, web), new Edge(web, text), new Edge(text, web), new Edge(text, core));
        final Placement placement = layering.of(cyclic);
        assertEquals(0, depth(placement, core));
        assertEquals(1, depth(placement, web));
        assertEquals(1, depth(placement, text));
        assertEquals(2, depth(placement, app));
    }

    @Test
    void aGraphWithNoEdgesIsOneLayer() {
        final Placement placement = layering.of(graph(List.of(app, core)));
        assertEquals(0, placement.maxDepth());
        assertEquals(List.of(0.0, 0.0), placement.items().stream().map(Placed::lane).toList());
        assertEquals(List.of(1, 0), placement.items().stream().map(Placed::row).toList());
    }

    @Test
    void theClosureIsTheRootAndWhatItReachesInTheGraphsOrder() {
        assertEquals(new Vertices(List.of(web, text, core)), new Closure().of(estate, web));
        assertEquals(new Vertices(List.of(json)), new Closure().of(estate, json));
    }

    @Test
    void theClosureOfAVertexOutsideTheGraphIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new Closure().of(estate, new Vertex("elsewhere")));
    }

    @Test
    void reachabilityLightsTheEdgesInsideTheClosureAndMarksNothing() {
        final Highlight highlight = new Reachability().of(estate, web);
        assertEquals(web, highlight.root());
        assertEquals(new Vertices(List.of(web, text, core)), highlight.members());
        assertEquals(new Edges(List.of(new Edge(web, text), new Edge(text, core))), highlight.lit());
        assertEquals(0, highlight.marked().size());
        assertEquals(0, highlight.markedEdges().size());
    }

    @Test
    void aHighlightWhoseRootIsNotAMemberIsRefused() {
        final Edges none = new Edges(List.of());
        assertThrows(IllegalArgumentException.class, () ->
                new Highlight(app, new Vertices(List.of(core)), new Vertices(List.of()), none, none));
    }

    @Test
    void aVertexBothAMemberAndMarkedIsRefused() {
        final Edges none = new Edges(List.of());
        assertThrows(IllegalArgumentException.class, () ->
                new Highlight(app, new Vertices(List.of(app, core)), new Vertices(List.of(core)), none, none));
    }

    @Test
    void aVertexPlacedTwiceIsRefused() {
        assertThrows(IllegalArgumentException.class, () ->
                new Placement(List.of(new Placed(app, 0, 0, 0), new Placed(app, 1, 0, 0))));
    }

    private Graph graph(final List<Vertex> vertices, final Edge... edges) {
        return new Graph(new Vertices(vertices), new Edges(Arrays.asList(edges)));
    }

    private int depth(final Placement placement, final Vertex vertex) {
        return placement.of(vertex).orElseThrow().depth();
    }

    private double lane(final Placement placement, final Vertex vertex) {
        return placement.of(vertex).orElseThrow().lane();
    }

    private int row(final Placement placement, final Vertex vertex) {
        return placement.of(vertex).orElseThrow().row();
    }
}
