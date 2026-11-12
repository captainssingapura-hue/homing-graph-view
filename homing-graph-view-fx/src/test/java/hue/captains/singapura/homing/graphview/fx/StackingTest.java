package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.layout.Arrangement;
import hue.captains.singapura.homing.graphview.layout.Block;
import hue.captains.singapura.homing.graphview.layout.LayerInBlock;
import hue.captains.singapura.homing.graphview.layout.Seat;
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
 * The flat layout set in 3D, standing and lying: each layer's block keeps its 2D arrangement in a plane of its own, no
 * two slates meet, and from the starting camera, looking straight in, no layer hides another.
 */
class StackingTest {

    private static final double PITCH = -24;
    private static final double CLOSE = 1e-9;

    /** Layers of 1, 30, 13, 8, 12, 5 and 1 vertices, the base's first: the JDK's modules, all of them. */
    private final List<Vertices> layers = layers(1, 30, 13, 8, 12, 5, 1);
    private final Stacking standing = new Stacking(new Stance.Standing(), PITCH);
    private final Stacking lying = new Stacking(new Stance.Lying(), PITCH);

    @Test
    void standingEachLayerIsAnUprightPlaneKeepingItsArrangement() {
        final Map<Vertex, Point3D> centres = standing.of(layers);
        for (final Vertices layer : layers) {
            final Arrangement flat = new LayerInBlock(standing.ladder()).of(layer);
            final double z = centres.get(layer.items().getFirst()).getZ();
            for (final Seat seat : flat.items()) {
                final Point3D centre = centres.get(seat.vertex());
                assertEquals(z, centre.getZ(), CLOSE, "one plane, facing the camera");
                assertEquals(seat.slot().lane() * Stacking.COLUMN, centre.getX(), CLOSE, "lanes across");
                assertEquals(-seat.slot().row() * Stacking.ROW, centre.getY(), CLOSE, "rows up from the floor");
            }
        }
    }

    @Test
    void lyingEachLayerIsAFloorKeepingItsArrangement() {
        final Map<Vertex, Point3D> centres = lying.of(layers);
        for (final Vertices layer : layers) {
            final Arrangement flat = new LayerInBlock(lying.ladder()).of(layer);
            final double y = centres.get(layer.items().getFirst()).getY();
            for (final Seat seat : flat.items()) {
                final Point3D centre = centres.get(seat.vertex());
                assertEquals(y, centre.getY(), CLOSE, "one level plane");
                assertEquals(seat.slot().lane() * Stacking.COLUMN, centre.getX(), CLOSE, "lanes across");
                assertEquals(seat.slot().row() * Stacking.ROW_DEEP, centre.getZ(), CLOSE, "rows going back");
            }
        }
    }

    @Test
    void standingTheRootIsInFrontAndLyingTheBaseIsAtTheBottom() {
        final List<Double> depthsBack = layers.stream().map(l -> standing.of(layers).get(l.items().getFirst()).getZ())
                .toList();
        final List<Double> heights = layers.stream().map(l -> -lying.of(layers).get(l.items().getFirst()).getY())
                .toList();
        for (int depth = 1; depth < layers.size(); depth++) {
            assertTrue(depthsBack.get(depth) < depthsBack.get(depth - 1), "a deeper layer stands in front");
            assertTrue(heights.get(depth) > heights.get(depth - 1), "a deeper layer lies above");
            assertTrue(depthsBack.get(depth - 1) - depthsBack.get(depth) >= Stacking.LEAST_GAP);
            assertTrue(heights.get(depth) - heights.get(depth - 1) >= Stacking.LEAST_GAP);
        }
    }

    @Test
    void eachStanceShapesItsBlocksForTheCamera() {
        // standing, 30 take the ladder's 5 by 8, cut to 6 rows; lying, slots are deeper and the floor deeper still
        assertEquals(new Block(5, 6), new LayerInBlock(standing.ladder()).of(layers.get(1)).block());
        assertEquals(new Block(4, 8), new LayerInBlock(lying.ladder()).of(layers.get(1)).block());
    }

    @Test
    void noTwoSlatesMeetInEitherStance() {
        for (final Stacking stacking : List.of(standing, lying)) {
            final List<Point3D> centres = new ArrayList<>(stacking.of(layers).values());
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
            final Map<Vertex, Point3D> centres = stacking.of(layers);
            // standing, the root's layer is nearest; lying, the base's: either way, each layer shows above the last
            final List<Vertices> nearestFirst = stacking.stance() instanceof Stance.Standing
                    ? layers.reversed() : layers;
            for (int i = 1; i < nearestFirst.size(); i++) {
                final double top = nearestFirst.get(i - 1).items().stream()
                        .mapToDouble(v -> centres.get(v).dotProduct(up) + reach)
                        .max().orElseThrow();
                final double bottom = nearestFirst.get(i).items().stream()
                        .mapToDouble(v -> centres.get(v).dotProduct(up) - reach)
                        .min().orElseThrow();
                assertTrue(bottom >= top, stacking.stance() + ": layer " + i + " from the front is hidden, "
                        + bottom + " below " + top);
            }
        }
    }

    private List<Vertices> layers(final int... sizes) {
        return IntStream.range(0, sizes.length).mapToObj(depth -> new Vertices(IntStream.range(0, sizes[depth])
                .mapToObj(i -> new Vertex("L" + depth + "." + i)).toList())).toList();
    }
}
