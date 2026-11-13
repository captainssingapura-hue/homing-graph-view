package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.layout.Highlight;
import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Edges;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.model.Vertices;
import javafx.collections.ObservableFloatArray;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Where each edge is drawn, how each texture and head is laid along it, and the colours a selection gives it. */
class EdgeDrawingTest {

    private static final double CLOSE = 1e-6;
    private static final double SIDE = 20;

    private final Vertex app = new Vertex("app");
    private final Vertex core = new Vertex("core");
    private final Edge uses = new Edge(app, core);
    private final Edge usedBy = new Edge(core, app);
    private final Point3D start = new Point3D(0, 0, 0);
    private final Point3D end = new Point3D(300, 0, 0);

    @Test
    void anEdgeRunsFromWhereItLeavesOneFigureToWhereItEntersTheOther() {
        final Spans across = spans(List.of(uses), new Point3D(0, 0, 0), new Point3D(100, 0, 0));
        assertEquals(new Point3D(10, 0, 0), across.start(uses));
        assertEquals(new Point3D(90, 0, 0), across.end(uses));
        final Spans aslant = spans(List.of(uses), new Point3D(0, 0, 0), new Point3D(100, 50, 0));
        assertEquals(10, aslant.start(uses).getX(), CLOSE, "out through the side, as the line is more across than up");
        assertEquals(5, aslant.start(uses).getY(), CLOSE);
        assertEquals(90, aslant.end(uses).getX(), CLOSE);
        assertEquals(45, aslant.end(uses).getY(), CLOSE);
    }

    @Test
    void aPairOfEdgesEachWaySplitsOneLineInTheMiddleEachOnTheSideOfTheVertexItDependsOn() {
        final Spans pair = spans(List.of(uses, usedBy), new Point3D(0, 0, 0), new Point3D(100, 0, 0));
        assertEquals(new Point3D(50, 0, 0), pair.start(uses));
        assertEquals(new Point3D(90, 0, 0), pair.end(uses));
        assertEquals(new Point3D(50, 0, 0), pair.start(usedBy));
        assertEquals(new Point3D(10, 0, 0), pair.end(usedBy));
    }

    @Test
    void figuresTooCloseToLeaveALineBetweenThemAreJoinedCentreToCentre() {
        final Spans close = spans(List.of(uses), new Point3D(0, 0, 0), new Point3D(15, 0, 0));
        assertEquals(new Point3D(0, 0, 0), close.start(uses));
        assertEquals(new Point3D(15, 0, 0), close.end(uses));
    }

    @Test
    void aSolidEdgeIsOneTubeAlongItsSpanAsThickAsItsGirth() {
        final StrandMesh plain = strand(new EdgeStyle());
        assertEquals(1, plain.pieces());
        assertEquals(0, least(plain.points(), 0), CLOSE);
        assertEquals(300, most(plain.points(), 0), CLOSE);
        assertEquals(1.8, across(plain.points()), 1e-5);
        assertEquals(0.6, across(strand(new EdgeStyle().with(new Girth(-1))).points()), 1e-5);
        assertEquals(5.4, across(strand(new EdgeStyle().with(new Girth(1))).points()), 1e-5);
    }

    @Test
    void dashesAndBeadsAreLaidEvenlyAlongTheSpanAndTwinsLieApartAboveAndBelow() {
        // dashes 26.8 long, 15.4 apart, the last cut short: 8 in 300
        assertEquals(8, strand(new EdgeStyle().with(new Texture.Dashed())).pieces());
        // beads 3.88 across, 21 apart from end to end: 14 in 300
        final StrandMesh beads = strand(new EdgeStyle().with(new Texture.Dotted()));
        assertEquals(14, beads.pieces());
        assertTrue(least(beads.points(), 0) >= 0 && most(beads.points(), 0) <= 300);
        final StrandMesh twins = strand(new EdgeStyle().with(new Texture.Twin()));
        assertEquals(2, twins.pieces());
        assertTrue(least(twins.points(), 1) < -1 && most(twins.points(), 1) > 1, "one above, one below");
        for (int point = 0; point < twins.points().size() / 3; point++) {
            assertTrue(Math.abs(twins.points().get(point * 3 + 1)) > 1, "a gap between the twins");
        }
    }

    @Test
    void anArrowheadEndsTheEdgeAtTheVertexDependedOnWiderThanTheEdge() {
        final StrandMesh arrow = strand(new EdgeStyle().with(new Head.Arrow()));
        assertEquals(2, arrow.pieces());
        assertEquals(300, most(arrow.points(), 0), CLOSE);
        assertTrue(across(arrow.points()) > 2 * 1.8, "the head is wider than the edge");
        final StrandMesh shortOne = new Strand().of(new EdgeStyle().with(new Head.Arrow()), start,
                new Point3D(10, 0, 0));
        assertTrue(least(shortOne.points(), 0) >= 0, "a head never longer than the edge");
        assertEquals(0, new Strand().of(new EdgeStyle(), start, start).pieces(), "nothing for no length");
    }

    @Test
    void anEdgeIsInItsOwnColourWithNothingSelectedAndOtherwiseInItsStatesColour() {
        final EdgeStyle gold = new EdgeStyle().with(new Colour(0xd4af37));
        final Rods rods = new Rods(new Edges(List.of(uses)),
                spans(List.of(uses), new Point3D(0, 0, 0), new Point3D(100, 0, 0)), new SameStyle(gold));
        final Palette palette = new Palette();
        rods.paint(Optional.empty(), new States(), palette);
        assertEquals(Color.web("#d4af37"), colour(rods));
        final Vertices both = new Vertices(List.of(app, core));
        final Vertices none = new Vertices(List.of());
        rods.paint(Optional.of(new Highlight(app, both, none, new Edges(List.of(uses)), new Edges(List.of()))),
                new States(), palette);
        assertEquals(palette.edge(new EdgeState.Lit()), colour(rods));
        rods.paint(Optional.of(new Highlight(core, new Vertices(List.of(core)), none, new Edges(List.of()),
                new Edges(List.of()))), new States(), palette);
        assertEquals(palette.edge(new EdgeState.Dim()), colour(rods));
    }

    private Spans spans(final List<Edge> edges, final Point3D at, final Point3D to) {
        return new Spans(new Edges(edges), Map.of(app, at, core, to), Map.of(app, box(at), core, box(to)));
    }

    private Bounds box(final Point3D centre) {
        return new BoundingBox(centre.getX() - SIDE / 2, centre.getY() - SIDE / 2, centre.getZ() - SIDE / 2, SIDE,
                SIDE, SIDE);
    }

    private StrandMesh strand(final EdgeStyle style) {
        return new Strand().of(style, start, end);
    }

    private Color colour(final Rods rods) {
        return ((PhongMaterial) ((MeshView) rods.node().getChildren().getFirst()).getMaterial()).getDiffuseColor();
    }

    /** The least of the points' coordinates on one axis: 0 for x, 1 for y. */
    private double least(final ObservableFloatArray points, final int axis) {
        double least = Double.POSITIVE_INFINITY;
        for (int point = 0; point < points.size() / 3; point++) {
            least = Math.min(least, points.get(point * 3 + axis));
        }
        return least;
    }

    private double most(final ObservableFloatArray points, final int axis) {
        double most = Double.NEGATIVE_INFINITY;
        for (int point = 0; point < points.size() / 3; point++) {
            most = Math.max(most, points.get(point * 3 + axis));
        }
        return most;
    }

    /** How far the points reach from the x axis, the line the edges here run along. */
    private double across(final ObservableFloatArray points) {
        double most = 0;
        for (int point = 0; point < points.size() / 3; point++) {
            most = Math.max(most, Math.hypot(points.get(point * 3 + 1), points.get(point * 3 + 2)));
        }
        return most;
    }
}
