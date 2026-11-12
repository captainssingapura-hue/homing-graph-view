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
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The layers by depth set in 3D, standing and lying: each layer's block keeps its arrangement in a plane of its own, no
 * two slates meet, and from the starting camera, looking straight in, no layer hides another.
 */
class StackingTest {

    private static final double PITCH = -24;
    private static final double CLOSE = 1e-9;

    /** Layers of 1, 30, 13, 8, 12, 5 and 1 vertices, the base's first: the JDK's modules, all of them. */
    private final Graph graph = graph(1, 30, 13, 8, 12, 5, 1);
    private final List<Vertices> layers = new Layers().of(graph);
    private final Stacking standing = new Stacking(new Stance.Standing(), PITCH);
    private final Stacking lying = new Stacking(new Stance.Lying(), PITCH);

    @Test
    void standingEachLayerIsAnUprightPlaneKeepingItsArrangement() {
        final Placement placement = placement(standing);
        final Map<Vertex, Point3D> centres = standing.of(placement);
        for (final Placed placed : placement.items()) {
            final Point3D centre = centres.get(placed.vertex());
            assertEquals(centreOf(centres, placed.depth()).getZ(), centre.getZ(), CLOSE, "one plane, facing us");
            assertEquals(placed.lane() * Stacking.COLUMN, centre.getX(), CLOSE, "lanes across");
            assertEquals(-placed.row() * Stacking.ROW, centre.getY(), CLOSE, "rows up from the floor");
        }
    }

    @Test
    void lyingEachLayerIsAFloorKeepingItsArrangement() {
        final Placement placement = placement(lying);
        final Map<Vertex, Point3D> centres = lying.of(placement);
        for (final Placed placed : placement.items()) {
            final Point3D centre = centres.get(placed.vertex());
            assertEquals(centreOf(centres, placed.depth()).getY(), centre.getY(), CLOSE, "one level plane");
            assertEquals(placed.lane() * Stacking.COLUMN, centre.getX(), CLOSE, "lanes across");
            assertEquals(placed.row() * Stacking.ROW_DEEP, centre.getZ(), CLOSE, "rows going back");
        }
    }

    @Test
    void standingTheRootIsInFrontAndLyingTheBaseIsAtTheBottom() {
        final Map<Vertex, Point3D> upright = standing.of(placement(standing));
        final Map<Vertex, Point3D> flat = lying.of(placement(lying));
        for (int depth = 1; depth < layers.size(); depth++) {
            final double nearer = centreOf(upright, depth - 1).getZ() - centreOf(upright, depth).getZ();
            final double higher = centreOf(flat, depth - 1).getY() - centreOf(flat, depth).getY();
            assertTrue(nearer >= Stacking.LEAST_GAP, "a deeper layer stands in front, at least a gap away");
            assertTrue(higher >= Stacking.LEAST_GAP, "a deeper layer lies above, at least a gap up");
        }
    }

    @Test
    void eachStanceShapesItsBlocksForTheCamera() {
        // standing, 30 take the ladder's 5 by 8, cut to 6 rows; lying, slots are deeper and the floor deeper still
        assertEquals(new Block(5, 6), new LayerInBlock(standing.ladder()).of(layers.get(1)).block());
        assertEquals(new Block(4, 8), new LayerInBlock(lying.ladder()).of(layers.get(1)).block());
        assertEquals(List.of(6, 8), List.of(placement(standing).rowsOf(1), placement(lying).rowsOf(1)));
    }

    @Test
    void noTwoSlatesMeetInEitherStance() {
        for (final Stacking stacking : List.of(standing, lying)) {
            final List<Point3D> centres = new ArrayList<>(stacking.of(placement(stacking)).values());
            for (int i = 0; i < centres.size(); i++) {
                for (int j = i + 1; j < centres.size(); j++) {
                    final Point3D apart = centres.get(i).subtract(centres.get(j));
                    assertTrue(Math.abs(apart.getX()) >= Slate.WIDTH || Math.abs(apart.getY()) >= Slate.HEIGHT
                            || Math.abs(apart.getZ()) >= Slate.DEPTH, stacking.stance() + ": slates " + i + ", " + j);
                }
            }
        }
    }

    @Test
    void fromTheStartingCameraLookingStraightInNoLayerHidesAnother() {
        final Point3D up = new Orbit(0, 0, 0, 0, PITCH, 1).down().multiply(-1);
        // how far a slate reaches up and down the screen from its centre: half its height, and half its thickness
        final double reach = Slate.HEIGHT / 2 * Math.abs(up.getY()) + Slate.DEPTH / 2 * Math.abs(up.getZ());
        for (final Stacking stacking : List.of(standing, lying)) {
            final Map<Vertex, Point3D> centres = stacking.of(placement(stacking));
            // standing, the root's layer is nearest; lying, the base's: either way, each layer shows above the last
            final List<Vertices> nearestFirst = stacking.stance() instanceof Stance.Standing
                    ? layers.reversed() : layers;
            for (int i = 1; i < nearestFirst.size(); i++) {
                final double top = nearestFirst.get(i - 1).items().stream()
                        .mapToDouble(v -> centres.get(v).dotProduct(up) + reach).max().orElseThrow();
                final double bottom = nearestFirst.get(i).items().stream()
                        .mapToDouble(v -> centres.get(v).dotProduct(up) - reach).min().orElseThrow();
                assertTrue(bottom >= top, stacking.stance() + ": layer " + i + " from the front is hidden, "
                        + bottom + " below " + top);
            }
        }
    }

    private Placement placement(final Stacking stacking) {
        return new LayersByDepth(stacking.ladder()).of(graph);
    }

    /** The centre of a layer's first slate. */
    private Point3D centreOf(final Map<Vertex, Point3D> centres, final int depth) {
        return centres.get(layers.get(depth).items().getFirst());
    }

    /** A graph whose layers have these sizes, the base's first: each vertex requires the first of the layer below. */
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
