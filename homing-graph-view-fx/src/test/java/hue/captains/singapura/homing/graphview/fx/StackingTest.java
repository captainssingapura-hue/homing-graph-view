package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.layout.Block;
import hue.captains.singapura.homing.graphview.layout.LayerInBlock;
import hue.captains.singapura.homing.graphview.layout.Layers;
import hue.captains.singapura.homing.graphview.layout.LayersByDepth;
import hue.captains.singapura.homing.graphview.layout.Placed;
import hue.captains.singapura.homing.graphview.layout.Placement;
import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Edges;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.model.Vertices;
import javafx.geometry.Point3D;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The layers set in 3D, standing and lying, level 0 at either end: each layer keeps its arrangement in a plane of its
 * own on a platform holding all its containers, nothing meets anything else, and from the starting camera, looking
 * straight in, no layer hides another.
 */
class StackingTest {

    private static final double PITCH = -24;
    private static final double CLOSE = 1e-9;
    /** The size of the largest container, which every container fits in. */
    private static final Point3D FULL = new Point3D(Slate.LENGTH, Slate.HEIGHT, Slate.DEPTH);

    /** Layers of 1, 30, 13, 8, 12, 5 and 1 vertices, level 0's first: the JDK's modules, all of them. */
    private final Graph graph = graph(1, 30, 13, 8, 12, 5, 1);
    private final List<Vertices> layers = new Layers().of(graph);
    private final Stacking standing = new Stacking(new Stance.Standing(), new Order.BaseFar(), PITCH);
    private final Stacking lying = new Stacking(new Stance.Lying(), new Order.BaseFar(), PITCH);
    private final List<Stacking> every = List.of(standing, lying,
            new Stacking(new Stance.Standing(), new Order.BaseNear(), PITCH),
            new Stacking(new Stance.Lying(), new Order.BaseNear(), PITCH));

    @Test
    void standingEachLayerIsAnUprightPlaneKeepingItsArrangement() {
        final Placement placement = placement(standing);
        final Map<Vertex, Point3D> centres = standing.of(placement);
        for (final Placed placed : placement.items()) {
            final Point3D centre = centres.get(placed.vertex());
            assertEquals(firstOf(centres, placed.depth()).getZ(), centre.getZ(), CLOSE, "one plane, facing us");
            assertEquals(placed.lane() * Stacking.COLUMN, centre.getX(), CLOSE, "lanes across");
            assertEquals(-middled(placement, placed) * Stacking.ROW, centre.getY(), CLOSE, "rows up, centred");
        }
    }

    @Test
    void lyingEachLayerIsAFloorKeepingItsArrangement() {
        final Placement placement = placement(lying);
        final Map<Vertex, Point3D> centres = lying.of(placement);
        for (final Placed placed : placement.items()) {
            final Point3D centre = centres.get(placed.vertex());
            assertEquals(firstOf(centres, placed.depth()).getY(), centre.getY(), CLOSE, "one level plane");
            assertEquals(placed.lane() * Stacking.COLUMN, centre.getX(), CLOSE, "lanes across");
            assertEquals(middled(placement, placed) * Stacking.ROW_DEEP, centre.getZ(), CLOSE,
                    "rows going back, centred");
        }
    }

    @Test
    void theOrderPutsLevelZeroAtTheFarEndOrTheNear() {
        final int top = layers.size() - 1;
        for (final Stacking stacking : every) {
            final Map<Vertex, Point3D> centres = stacking.of(placement(stacking));
            final boolean far = stacking.order() instanceof Order.BaseFar;
            if (stacking.stance() instanceof Stance.Standing) {
                // far is the back, larger z; near is the front
                assertEquals(far, firstOf(centres, 0).getZ() > firstOf(centres, top).getZ(), stacking.toString());
            } else {
                // far is the bottom, larger y as y points down; near is the top
                assertEquals(far, firstOf(centres, 0).getY() > firstOf(centres, top).getY(), stacking.toString());
            }
        }
    }

    @Test
    void eachLayersPlatformHoldsAllItsContainers() {
        for (final Stacking stacking : every) {
            final Placement placement = placement(stacking);
            final Map<Vertex, Point3D> centres = stacking.of(placement);
            for (final Deck deck : stacking.decks(placement)) {
                for (final Vertex vertex : layers.get(deck.depth()).items()) {
                    final Point3D low = centres.get(vertex).subtract(FULL.multiply(0.5));
                    final Point3D high = centres.get(vertex).add(FULL.multiply(0.5));
                    assertTrue(deck.low().getX() < low.getX() && high.getX() < deck.high().getX(), "across");
                    if (stacking.stance() instanceof Stance.Standing) {
                        assertTrue(deck.low().getY() < low.getY() && high.getY() < deck.high().getY(), "up");
                        assertEquals(high.getZ(), deck.low().getZ(), CLOSE, "the board right behind it");
                    } else {
                        assertTrue(deck.low().getZ() < low.getZ() && high.getZ() < deck.high().getZ(), "back");
                        assertEquals(high.getY(), deck.low().getY(), CLOSE, "sitting on the floor");
                    }
                }
            }
        }
    }

    @Test
    void everyLayerIsCentredOnTheStacksAxisSoItLooksEvenFromTheSide() {
        for (final Stacking stacking : every) {
            final Placement placement = placement(stacking);
            final boolean standing = stacking.stance() instanceof Stance.Standing;
            for (final Deck deck : stacking.decks(placement)) {
                // the axis runs back through the stack when standing, up it when lying
                assertEquals(0, deck.x(), CLOSE, stacking + ": layer " + deck.depth() + " across");
                assertEquals(0, standing ? deck.y() : deck.z(), CLOSE, stacking + ": layer " + deck.depth());
            }
            final Map<Vertex, Point3D> centres = stacking.of(placement);
            for (final Vertices layer : layers) {
                final double highest = layer.items().stream().map(centres::get)
                        .mapToDouble(centre -> standing ? -centre.getY() : centre.getZ()).max().orElseThrow();
                final double lowest = layer.items().stream().map(centres::get)
                        .mapToDouble(centre -> standing ? -centre.getY() : centre.getZ()).min().orElseThrow();
                assertEquals(0, highest + lowest, CLOSE, stacking + ": rows reach as far either side");
            }
        }
    }

    @Test
    void eachStanceShapesItsBlocksLikeAScreenAndTurnsItsCamera() {
        // standing, 30 take the ladder's 5 by 8, cut to the 6 rows filled; lying, 6 by 5, a screen laid flat
        assertEquals(new Block(5, 6), new LayerInBlock(standing.ladder()).of(layers.get(1)).block());
        assertEquals(new Block(6, 5), new LayerInBlock(lying.ladder()).of(layers.get(1)).block());
        assertEquals(List.of(-28.0, 0.0), List.of(standing.yaw(), lying.yaw()));
    }

    @Test
    void nothingMeetsAnythingElse() {
        for (final Stacking stacking : every) {
            final Placement placement = placement(stacking);
            final List<Point3D[]> boxes = new ArrayList<>();
            stacking.of(placement).values().forEach(centre -> boxes.add(new Point3D[] {
                    centre.subtract(FULL.multiply(0.5)), centre.add(FULL.multiply(0.5))}));
            stacking.decks(placement).forEach(deck -> boxes.add(new Point3D[] {deck.low(), deck.high()}));
            // a container sits on its platform, or hangs on it, face to face: touching is not meeting
            for (int i = 0; i < boxes.size(); i++) {
                for (int j = i + 1; j < boxes.size(); j++) {
                    assertFalse(overlap(boxes.get(i), boxes.get(j)), stacking + ": boxes " + i + " and " + j);
                }
            }
        }
    }

    @Test
    void fromTheStartingCameraLookingStraightInNoLayerHidesAnother() {
        final Point3D up = new Orbit(0, 0, 0, 0, PITCH, 1).down().multiply(-1);
        for (final Stacking stacking : every) {
            final Placement placement = placement(stacking);
            final Map<Vertex, Point3D> centres = stacking.of(placement);
            final List<Deck> decks = stacking.decks(placement);
            // in the stack's order, from the front standing and from the bottom lying, each layer shows above the last
            final List<Integer> order = IntStream.range(0, layers.size()).boxed()
                    .sorted(Comparator.comparingDouble(depth -> stacking.stance() instanceof Stance.Standing
                            ? firstOf(centres, depth).getZ() : -firstOf(centres, depth).getY()))
                    .toList();
            for (int i = 1; i < order.size(); i++) {
                final double[] before = reach(order.get(i - 1), centres, decks, up);
                final double[] after = reach(order.get(i), centres, decks, up);
                assertTrue(after[0] >= before[1], stacking + ": layer " + order.get(i) + " is hidden, "
                        + after[0] + " below " + before[1]);
            }
        }
    }

    @Test
    void containersComeInStandardSizesThatAllFitOneSlot() {
        final List<Container> sizes = List.of(new Container.Small(), new Container.Half(), new Container.Full());
        assertEquals(List.of(0.25, 0.5, 1.0), sizes.stream().map(Container::length).toList());
        assertTrue(Slate.LENGTH <= Stacking.COLUMN && Slate.HEIGHT <= Stacking.ROW
                && Slate.DEPTH <= Stacking.ROW_DEEP, "a full container fits a slot either way");
        assertEquals(new Container.Half(), new Uniform(new Container.Half()).of(new Vertex("any")));
        assertEquals(new Staging(new Stance.Lying(), new Order.BaseNear(), new Uniform(new Container.Full())),
                new Staging().with(new Stance.Lying()).with(new Order.BaseNear()));
    }

    @Test
    void neighbouringLayersPlatformsDifferInColour() {
        final Palette palette = new Palette();
        for (int depth = 0; depth < 24; depth++) {
            assertNotEquals(palette.platform(depth), palette.platform(depth + 1), "layers " + depth + " and above");
        }
    }

    /** How low and how high on the screen a layer reaches: its containers' and its platform's corners. */
    private double[] reach(final int depth, final Map<Vertex, Point3D> centres, final List<Deck> decks,
                           final Point3D up) {
        final List<Point3D> corners = new ArrayList<>(corners(decks.get(depth).low(), decks.get(depth).high()));
        layers.get(depth).items().forEach(vertex -> corners.addAll(corners(
                centres.get(vertex).subtract(FULL.multiply(0.5)), centres.get(vertex).add(FULL.multiply(0.5)))));
        return new double[] {corners.stream().mapToDouble(up::dotProduct).min().orElseThrow(),
                corners.stream().mapToDouble(up::dotProduct).max().orElseThrow()};
    }

    private List<Point3D> corners(final Point3D low, final Point3D high) {
        final List<Point3D> corners = new ArrayList<>();
        for (final double x : new double[] {low.getX(), high.getX()}) {
            for (final double y : new double[] {low.getY(), high.getY()}) {
                for (final double z : new double[] {low.getZ(), high.getZ()}) {
                    corners.add(new Point3D(x, y, z));
                }
            }
        }
        return corners;
    }

    private boolean overlap(final Point3D[] one, final Point3D[] other) {
        return one[0].getX() < other[1].getX() - CLOSE && other[0].getX() < one[1].getX() - CLOSE
                && one[0].getY() < other[1].getY() - CLOSE && other[0].getY() < one[1].getY() - CLOSE
                && one[0].getZ() < other[1].getZ() - CLOSE && other[0].getZ() < one[1].getZ() - CLOSE;
    }

    /** How many rows a placed vertex is from the middle of its layer's rows. */
    private double middled(final Placement placement, final Placed placed) {
        return placed.row() - (placement.rowsOf(placed.depth()) - 1) / 2.0;
    }

    private Placement placement(final Stacking stacking) {
        return new LayersByDepth(stacking.ladder()).of(graph);
    }

    /** The centre of a layer's first container. */
    private Point3D firstOf(final Map<Vertex, Point3D> centres, final int depth) {
        return centres.get(layers.get(depth).items().getFirst());
    }

    /** A graph whose layers have these sizes, level 0's first: each vertex requires the first of the layer below. */
    private Graph graph(final int... sizes) {
        final List<List<Vertex>> made = IntStream.range(0, sizes.length).mapToObj(depth -> IntStream
                .range(0, sizes[depth]).mapToObj(i -> new Vertex("L" + depth + "." + i)).toList()).toList();
        final List<Edge> edges = new ArrayList<>();
        for (int depth = 1; depth < made.size(); depth++) {
            final Vertex below = made.get(depth - 1).getFirst();
            made.get(depth).forEach(vertex -> edges.add(new Edge(vertex, below)));
        }
        return new Graph(new Vertices(made.stream().flatMap(List::stream).toList()), new Edges(edges));
    }
}
