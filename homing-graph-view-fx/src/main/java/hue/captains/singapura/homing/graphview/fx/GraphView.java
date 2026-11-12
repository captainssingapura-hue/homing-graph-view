package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.layout.Focus;
import hue.captains.singapura.homing.graphview.layout.Highlight;
import hue.captains.singapura.homing.graphview.layout.LayersByDepth;
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
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Subscription;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A graph drawn in 3D, as an ordinary JavaFX node to put in any scene. Each vertex is a figure, a container unless
 * the staging's shapes say otherwise, its layer by depth laid out flat in a block on a platform coloured for the layer,
 * and the layers stacked as a {@link Staging} says: standing one behind another or lying one above another, level 0
 * at either end; all over a grid on the floor. Each edge is a rod. The camera starts orbiting, framed on the whole
 * graph, and can fly or drone through it; a readout in the corner says which, what the keys do and what is selected.
 * What a selection means is asked of a Focus, reachability unless another is given, and each change of it goes out as
 * a {@link VertexSelection} to the listeners subscribed.
 */
public final class GraphView extends Region implements Mutable {

    private static final double PITCH = -24;
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
    private final SelectionFeed feed = new SelectionFeed();
    private final double yaw;
    private final Room room;
    private Optional<Highlight> selection = Optional.empty();
    private boolean framed;

    /** A view of the graph, with reachability as its focus, staged as {@link Staging#Staging()} says. */
    public GraphView(final Graph graph) {
        this(graph, new Reachability());
    }

    /**
     * A view of the graph, staged as {@link Staging#Staging()} says: standing, level 0 at the back, every vertex in a
     * full container.
     *
     * @param graph the graph to draw
     * @param focus what a selection means
     */
    public GraphView(final Graph graph, final Focus focus) {
        this(graph, focus, new Staging());
    }

    /**
     * A view of the graph.
     *
     * @param graph   the graph to draw
     * @param focus   what a selection means
     * @param staging how the layers are set and stacked, and what each vertex is drawn as
     */
    public GraphView(final Graph graph, final Focus focus, final Staging staging) {
        this.graph = graph;
        this.focus = focus;
        final Shapes shapes = staging.shapes();
        final Stacking stacking = new Stacking(staging.stance(), staging.order(), PITCH, shapes.room());
        final Placement placement = new LayersByDepth(stacking.ladder()).of(graph);
        final Map<Vertex, Point3D> placed = stacking.of(placement);
        placed.forEach((vertex, centre) -> slates.put(vertex, new Slate(vertex, centre, shapes.of(vertex))));
        room = shapes.room();
        yaw = stacking.yaw();
        centres.addAll(placed.values());
        rods = new Rods(graph.edges(), placed);
        final Grounds grounds = new Grounds(centres, stacking.below());
        final Platforms platforms = new Platforms(stacking.decks(placement), palette);
        final Group world = new Group(new Grid(grounds).node(), platforms.node(), rods.node(),
                new AmbientLight(Color.gray(0.6)), rig.node());
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

    /** Selects a vertex of the graph: the focus says what it lights, and every listener hears of it. */
    public void select(final Vertex vertex) {
        selection = Optional.of(focus.of(graph, vertex));
        paint();
        showReadout();
        feed.emit(new VertexSelection.Some(vertex));
    }

    /** Clears the selection, and every listener hears that nothing is selected. */
    public void clear() {
        selection = Optional.empty();
        paint();
        showReadout();
        feed.emit(new VertexSelection.Nothing());
    }

    /**
     * Listens to the selection: from now on, the listener hears each change of it, some vertex selected or nothing, by
     * a click, by Esc or by {@link #select} and {@link #clear}. Nothing is selected to begin with.
     *
     * @param listener told of each change
     * @return what ends the listening
     */
    public Subscription subscribe(final Consumer<? super VertexSelection> listener) {
        return feed.subscribe(listener);
    }

    /** What is selected: some vertex, or nothing. */
    public VertexSelection selected() {
        return feed.current();
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
        readout.setText(navigator.readout() + selection.map(lit -> "\nselected: " + lit.root().id()).orElse(""));
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
        final double reach = Math.max(room.width(), room.height()) / 2 + Stacking.MARGIN;
        return new Framing().of(centres, reach, yaw, PITCH, rig.fieldOfView(),
                getWidth() / Math.max(getHeight(), 1));
    }

    private void paint() {
        slates.forEach((vertex, slate) -> slate.paint(states.of(selection, vertex), palette));
        rods.paint(selection, states, palette);
    }
}
