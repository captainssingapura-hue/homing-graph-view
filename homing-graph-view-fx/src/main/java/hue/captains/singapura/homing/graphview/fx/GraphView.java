package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.layout.Focus;
import hue.captains.singapura.homing.graphview.layout.Highlight;
import hue.captains.singapura.homing.graphview.layout.LayersByDepth;
import hue.captains.singapura.homing.graphview.layout.Placed;
import hue.captains.singapura.homing.graphview.layout.Placement;
import hue.captains.singapura.homing.graphview.layout.Reachability;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.Mutable;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A graph drawn in 3D, as an ordinary JavaFX node to put in any scene. Each vertex is a slate in layers by depth, the
 * base layer furthest back; each edge is a rod. The camera orbits the graph and starts framed on all of it. What a
 * selection means is asked of a Focus, reachability unless another is given.
 */
public final class GraphView extends Region implements Mutable {

    private static final double COLUMN = 230;
    private static final double LAYER = 300;
    private static final double ROW = 70;
    private static final double YAW = -28;
    private static final double PITCH = -24;
    private static final double FONT_SIZE = 15;

    private final Graph graph;
    private final Focus focus;
    private final Palette palette = new Palette();
    private final States states = new States();
    private final CameraRig rig = new CameraRig();
    private final Map<Vertex, Slate> slates = new LinkedHashMap<>();
    private final List<Point3D> centres = new ArrayList<>();
    private final Rods rods;
    private final SubScene scene;
    private Optional<Highlight> selection = Optional.empty();
    private boolean framed;

    /** A view of the graph, with reachability as its focus. */
    public GraphView(final Graph graph) {
        this(graph, new Reachability());
    }

    /**
     * A view of the graph.
     *
     * @param graph the graph to draw
     * @param focus what a selection means
     */
    public GraphView(final Graph graph, final Focus focus) {
        this.graph = graph;
        this.focus = focus;
        final Map<Vertex, Point3D> placed = centres(new LayersByDepth().of(graph));
        final Font font = Font.font(FONT_SIZE);
        placed.forEach((vertex, centre) -> slates.put(vertex, new Slate(vertex, centre, font)));
        centres.addAll(placed.values());
        rods = new Rods(graph.edges(), placed);
        final Group world = new Group(rods.node(), new AmbientLight(Color.gray(0.6)), rig.node());
        slates.values().forEach(slate -> world.getChildren().add(slate.node()));
        scene = new SubScene(world, 1, 1, true, SceneAntialiasing.BALANCED);
        scene.setFill(palette.background());
        scene.setCamera(rig.camera());
        getChildren().add(scene);
        setFocusTraversable(true);
        new Gestures(this, rig, scene).attach();
        paint();
    }

    /** Selects a vertex of the graph: the focus says what it lights. */
    public void select(final Vertex vertex) {
        selection = Optional.of(focus.of(graph, vertex));
        paint();
    }

    /** Clears the selection. */
    public void clear() {
        selection = Optional.empty();
        paint();
    }

    /** The selection, empty when nothing is selected. */
    public Optional<Highlight> selection() {
        return selection;
    }

    /** Glides back to the whole graph, framed from the starting angle. */
    public void home() {
        rig.glideTo(framing());
    }

    @Override
    protected void layoutChildren() {
        scene.setWidth(getWidth());
        scene.setHeight(getHeight());
        if (!framed && getWidth() > 0 && getHeight() > 0) {
            framed = true;
            rig.show(framing());
        }
    }

    @Override
    protected double computePrefWidth(final double height) {
        return 960;
    }

    @Override
    protected double computePrefHeight(final double width) {
        return 640;
    }

    private Orbit framing() {
        return new Framing().of(centres, Slate.WIDTH / 2, YAW, PITCH, rig.fieldOfView(),
                getWidth() / Math.max(getHeight(), 1));
    }

    private void paint() {
        slates.forEach((vertex, slate) -> slate.paint(states.of(selection, vertex), palette));
        rods.paint(selection, states, palette);
    }

    /** Where each slate is centred: its lane across, its row up and its layer back, the base layer furthest. */
    private Map<Vertex, Point3D> centres(final Placement placement) {
        final Map<Vertex, Point3D> placed = new LinkedHashMap<>();
        for (final Placed place : placement.items()) {
            placed.put(place.vertex(), new Point3D(place.lane() * COLUMN, -place.row() * ROW,
                    (placement.maxDepth() - place.depth()) * LAYER));
        }
        return placed;
    }
}
