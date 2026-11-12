package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Edges;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.model.Vertices;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/** The ladder of block sizes, a graph's layers, and one layer arranged in a block: the flat, 2D layout. */
class BlocksTest {

    /** A slot of 230 by 80 on a 16:9 screen, each size half as big again, as an ArrayList grows. */
    private final Ladder monitor = new Ladder(230.0 / 80, 16.0 / 9, 1.5);

    @Test
    void theLadderStartsAtOneSlotAndGrowsByHalfAgainInTheScreensShape() {
        assertEquals(List.of(new Block(1, 1), new Block(1, 2), new Block(2, 3), new Block(3, 5), new Block(4, 6),
                new Block(5, 8), new Block(6, 10), new Block(8, 13), new Block(10, 16)), monitor.sizes(160));
        final List<Block> sizes = monitor.sizes(100_000);
        for (int i = 1; i < sizes.size(); i++) {
            assertTrue(sizes.get(i).capacity() >= 1.5 * sizes.get(i - 1).capacity(), "after " + sizes.get(i - 1));
        }
        // past the first few, every size is within a tenth of 16:9
        sizes.stream().skip(3).forEach(block -> assertEquals(16.0 / 9,
                block.columns() * 230.0 / (block.rows() * 80), 16.0 / 90, block.toString()));
    }

    @Test
    void aLayerTakesTheSmallestSizeThatHoldsIt() {
        assertEquals(new Block(1, 1), monitor.holding(0));
        assertEquals(new Block(1, 1), monitor.holding(1));
        assertEquals(new Block(1, 2), monitor.holding(2));
        assertEquals(new Block(3, 5), monitor.holding(9));
        assertEquals(new Block(3, 5), monitor.holding(15));
        assertEquals(new Block(4, 6), monitor.holding(16));
        assertEquals(new Block(5, 8), monitor.holding(30));
    }

    @Test
    void aLadderThatDoesNotGrowOrHasNoShapeIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new Ladder(2, 16.0 / 9, 1));
        assertThrows(IllegalArgumentException.class, () -> new Ladder(0, 16.0 / 9, 1.5));
        assertThrows(IllegalArgumentException.class, () -> new Ladder(2, Double.NaN, 1.5));
        assertThrows(IllegalArgumentException.class, () -> monitor.holding(-1));
        assertThrows(IllegalArgumentException.class, () -> new Block(0, 3));
    }

    @Test
    void theBlockIsAsWideAsTheLadderSaysAndAsTallAsTheLayerFills() {
        assertEquals(new Block(3, 3), arranged(9).block(), "the ladder's 3 by 5, three rows filled");
        assertEquals(new Block(4, 4), arranged(16).block(), "the ladder's 4 by 6, four rows filled");
        assertEquals(new Block(5, 6), arranged(30).block(), "the ladder's 5 by 8, six rows filled");
        assertEquals(new Block(1, 2), arranged(2).block());
        assertEquals(new Block(1, 1), arranged(1).block());
        assertEquals(new Block(1, 1), arranged(0).block());
        assertEquals(0, arranged(0).size());
    }

    @Test
    void aLayerFillsItsBlockAsAListFillsAnArrayStandingOnTheBottom() {
        final Arrangement arrangement = arranged(9);
        assertEquals(3, arrangement.rows());
        // reading order: the first three across the top, the last three on the bottom
        assertEquals(List.of(-1.0, 0.0, 1.0, -1.0, 0.0, 1.0, -1.0, 0.0, 1.0), lanes(arrangement));
        assertEquals(List.of(2, 2, 2, 1, 1, 1, 0, 0, 0), rows(arrangement));
    }

    @Test
    void aLastRowThatIsNotFullIsCentred() {
        final Arrangement arrangement = arranged(8);
        assertEquals(List.of(-1.0, 0.0, 1.0, -1.0, 0.0, 1.0, -0.5, 0.5), lanes(arrangement));
        assertEquals(List.of(2, 2, 2, 1, 1, 1, 0, 0), rows(arrangement));
    }

    @Test
    void theArrangementKeepsTheLayersOrderAndFindsEachVertex() {
        final List<Vertex> seven = vertices(7);
        final Arrangement arrangement = new LayerInBlock(monitor).of(new Vertices(seven));
        assertEquals(seven, arrangement.items().stream().map(Seat::vertex).toList());
        assertEquals(new Slot(0, 0), arrangement.of(seven.get(6)).orElseThrow().slot());
        assertTrue(arrangement.of(new Vertex("elsewhere")).isEmpty());
    }

    @Test
    void aSeatOutsideTheBlockOrAVertexSeatedTwiceIsRefused() {
        final Vertex lone = new Vertex("lone");
        final Block block = new Block(3, 2);
        assertDoesNotThrow(() -> new Arrangement(block, List.of(new Seat(lone, new Slot(1, 1)))));
        assertThrows(IllegalArgumentException.class, () ->
                new Arrangement(block, List.of(new Seat(lone, new Slot(1.5, 0)))));
        assertThrows(IllegalArgumentException.class, () ->
                new Arrangement(block, List.of(new Seat(lone, new Slot(0, 2)))));
        assertThrows(IllegalArgumentException.class, () -> new Arrangement(block,
                List.of(new Seat(lone, new Slot(0, 0)), new Seat(lone, new Slot(1, 0)))));
    }

    @Test
    void theLayersAreTheDepthsTheBaseFirstEachInTheGraphsOrder() {
        final Vertex app = new Vertex("app");
        final Vertex web = new Vertex("web");
        final Vertex text = new Vertex("text");
        final Vertex core = new Vertex("core");
        final Vertex json = new Vertex("json");
        final Graph graph = new Graph(new Vertices(List.of(app, web, text, core, json)), new Edges(List.of(
                new Edge(app, web), new Edge(app, core), new Edge(web, text), new Edge(text, web),
                new Edge(text, core))));
        assertEquals(List.of(new Vertices(List.of(core, json)), new Vertices(List.of(web, text)),
                new Vertices(List.of(app))), new Layers().of(graph));
        assertEquals(List.of(), new Layers().of(new Graph(new Vertices(List.of()), new Edges(List.of()))));
    }

    private Arrangement arranged(final int count) {
        return new LayerInBlock(monitor).of(new Vertices(vertices(count)));
    }

    private List<Vertex> vertices(final int count) {
        return IntStream.range(0, count).mapToObj(i -> new Vertex("v" + i)).toList();
    }

    private List<Double> lanes(final Arrangement arrangement) {
        return arrangement.items().stream().map(seat -> seat.slot().lane()).toList();
    }

    private List<Integer> rows(final Arrangement arrangement) {
        return arrangement.items().stream().map(seat -> seat.slot().row()).toList();
    }
}
