package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.layout.Focus;
import hue.captains.singapura.homing.graphview.layout.Highlight;
import hue.captains.singapura.homing.graphview.layout.Layers;
import hue.captains.singapura.homing.graphview.layout.Reachability;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.Mutable;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A graph drawn in 3D, as an ordinary JavaFX node to put in any scene. Each vertex is a slate, its layer by depth laid
 * out flat in a block, and the blocks set in a stance: standing one behind another, the base's furthest back, or lying
 * one above another, the base's at the bottom; all over a grid on the floor. Each edge is a rod. The camera starts
 * orbiting, framed on the whole graph, and can fly or drone through it; a readout in the corner says which, and what
 * the keys do. What a selection means is asked of a Focus, reachability unless another is given.
 */
public final class GraphView extends Region implements Mutable {

    private static final double YAW = -28;
    private static final double PITCH = -24;
    private static final double FONT_SIZE = 15;
    private static final double READOUT_INSET = 10;

    private final Graph graph;
    private final Focus focus;
    private final Palette palette = new Palette();
    private final States states = new States();
    private final CameraRig rig = new CameraRig();
    private final Map<Vertex, Slate> slates = new LinkedHashMap<>();
    private final List<Point3D> centres = new ArrayList<>();
    private final Rods rods;
    private final SubScene scene;
    private final Navigator navigator;
    private final Label readout = new Label();
    private Optional<Highlight> selection = Optional.empty();
    private boolean framed;

    /** A view of the graph, with reachability as its focus and its layers standing. */
    public GraphView(final Graph graph) {
        this(graph, new Reachability());
    }

    /**
     * A view of the graph, its layers standing.
     *
     * @param graph the graph to draw
     * @param focus what a selection means
     */
    public GraphView(final Graph graph, final Focus focus) {
        this(graph, focus, new Stance.Standing());
    }

    /**
     * A view of the graph.
     *
     * @param graph  the graph to draw
     * @param focus  what a selection means
     * @param stance how each layer's block is set: standing or lying
     */
    public GraphView(final Graph graph, final Focus focus, final Stance stance) {
        this.graph = graph;
        this.focus = focus;
        final Map<Vertex, Point3D> placed = new Stacking(stance, PITCH).of(new Layers().of(graph));
        final Font font = Font.font(FONT_SIZE);
        placed.forEach((vertex, centre) -> slates.put(vertex, new Slate(vertex, centre, font)));
        centres.addAll(placed.values());
        rods = new Rods(graph.edges(), placed);
        final Grounds grounds = new Grounds(centres, Slate.HEIGHT / 2);
        final Group world = new Group(new Grid(grounds).node(), rods.node(), new AmbientLight(Color.gray(0.6)),
                rig.node());
        slates.values().forEach(slate -> world.getChildren().add(slate.node()));
        scene = new SubScene(world, 1, 1, true, SceneAntialiasing.BALANCED);
        scene.setFill(palette.background());
        scene.setCamera(rig.camera());
        navigator = new Navigator(rig, grounds, this::framing, this::showReadout);
        readout.setMouseTransparent(true);
        readout.setStyle("-fx-background-color: #0d0f13cc; -fx-text-fill: #9aa3b2; -fx-padding: 6 10 6 10;"
                + " -fx-background-radius: 4;");
        getChildren().addAll(scene, readout);
        setFocusTraversable(true);
        new Gestures(this, navigator, scene).attach();
        showReadout();
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

    /** Glides back to the whole graph, framed from the starting angle, orbiting again if flying. */
    public void home() {
        navigator.home();
    }

    @Override
    protected void layoutChildren() {
        scene.setWidth(getWidth());
        scene.setHeight(getHeight());
        readout.resizeRelocate(READOUT_INSET, READOUT_INSET, readout.prefWidth(-1), readout.prefHeight(-1));
        if (!framed && getWidth() > 0 && getHeight() > 0) {
            framed = true;
            rig.show(framing());
        }
    }

    private void showReadout() {
        readout.setText(navigator.readout());
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
}
