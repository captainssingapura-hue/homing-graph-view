package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.homing.graphview.fx.GraphView;
import hue.captains.singapura.homing.graphview.fx.Order;
import hue.captains.singapura.homing.graphview.fx.Stance;
import hue.captains.singapura.homing.graphview.fx.Staging;
import hue.captains.singapura.homing.graphview.layout.Reachability;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.tao.ontology.Mutable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The demo's window: down the left, a list of the demos and a choice of layout, horizontal or vertical; beside them,
 * the demo picked, drawn in the layout chosen. Picking another demo or layout draws it afresh in place of the last.
 * The arguments say where to start: royal or a JDK module, which joins the list if it is not on it; lying for the
 * vertical layout; and near to put level 0 at the near end of the stack, in front or on top.
 */
final class Showcase implements Mutable {

    private static final String LYING = "lying";
    private static final String NEAR = "near";
    private static final double SIDE_WIDTH = 220;
    private static final double SPACING = 6;
    private static final double PADDING = 10;
    private static final String HEADING = "-fx-text-fill: #9aa3b2; -fx-font-weight: bold;";

    private final Set<String> words = Set.of(LYING, NEAR, "standing", "far");
    private final BorderPane root = new BorderPane();
    private final ListView<String> list = new ListView<>();
    private final RadioButton horizontal = new RadioButton("Horizontal");
    private final RadioButton vertical = new RadioButton("Vertical");
    private final List<Exhibit> exhibits = new ArrayList<>(List.of(new Exhibit.Modules("java.se"),
            new Exhibit.Modules("java.desktop"), new Exhibit.Modules("jdk.jshell"), new Exhibit.Modules("ALL-SYSTEM"),
            new Exhibit.Royals()));
    private final Consumer<String> title;
    private Staging staging;

    /**
     * The window's content, on the demo and layout the arguments pick.
     *
     * @param args  the demo's arguments: lying, near, and royal or a JDK module, in any order
     * @param title where to say what is shown, such as the window's title
     */
    Showcase(final List<String> args, final Consumer<String> title) {
        this.title = title;
        final boolean lying = args.contains(LYING);
        this.staging = new Staging().with(lying ? new Stance.Lying() : new Stance.Standing())
                .with(args.contains(NEAR) ? new Order.BaseNear() : new Order.BaseFar());
        final int first = start(args.stream().filter(arg -> !words.contains(arg)).findFirst().orElse("java.se"));
        list.getItems().setAll(exhibits.stream().map(Exhibit::name).toList());
        list.getSelectionModel().selectedIndexProperty().addListener((property, before, after) -> redraw());
        final ToggleGroup layouts = new ToggleGroup();
        horizontal.setToggleGroup(layouts);
        vertical.setToggleGroup(layouts);
        layouts.selectToggle(lying ? vertical : horizontal);
        layouts.selectedToggleProperty().addListener((property, before, after) -> {
            staging = staging.with(after == vertical ? new Stance.Lying() : new Stance.Standing());
            redraw();
        });
        root.setLeft(side());
        list.getSelectionModel().select(first);
    }

    /** The window's content. */
    BorderPane node() {
        return root;
    }

    /** The side panel: the list of demos, and under it the choice of layout. */
    private VBox side() {
        final Label demos = new Label("Demos");
        demos.setStyle(HEADING);
        list.setStyle("-fx-control-inner-background: #1a1e25; -fx-background-color: #1a1e25;");
        VBox.setVgrow(list, Priority.ALWAYS);
        final Label layout = new Label("Layout");
        layout.setStyle(HEADING);
        final Label how = new Label("Horizontal: the layers one behind another. Vertical: one above another.");
        how.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
        how.setWrapText(true);
        List.of(horizontal, vertical).forEach(button -> button.setStyle("-fx-text-fill: #c9d1dc;"));
        final VBox side = new VBox(SPACING, demos, list, layout, horizontal, vertical, how);
        VBox.setMargin(layout, new Insets(PADDING, 0, 0, 0));
        side.setPadding(new Insets(PADDING));
        side.setPrefWidth(SIDE_WIDTH);
        side.setStyle("-fx-background-color: #14171c;");
        return side;
    }

    /** Which demo to start on, by its place in the list: a module not on the list joins it at the top. */
    private int start(final String asked) {
        if ("royal".equalsIgnoreCase(asked)) {
            return exhibits.indexOf(new Exhibit.Royals());
        }
        final Exhibit.Modules modules = new Exhibit.Modules(asked);
        if (!exhibits.contains(modules)) {
            exhibits.addFirst(modules);
        }
        return exhibits.indexOf(modules);
    }

    /** Draws the demo picked, in the layout chosen, in place of the last; or says why it cannot. */
    private void redraw() {
        final int picked = list.getSelectionModel().getSelectedIndex();
        if (picked < 0) {
            return;
        }
        final Exhibit exhibit = exhibits.get(picked);
        try {
            final Graph graph = exhibit.graph();
            final GraphView view = new GraphView(graph, new Reachability(), staging.with(exhibit.shapes()));
            root.setCenter(view);
            title.accept("homing-graph-view demo: " + exhibit.title(graph) + ", " + where());
            view.requestFocus();
        } catch (final IllegalArgumentException refused) {
            final Label why = new Label(refused.getMessage());
            why.setStyle("-fx-text-fill: #e0a030; -fx-padding: 20;");
            root.setCenter(why);
            title.accept("homing-graph-view demo: " + refused.getMessage());
        }
    }

    /** The layout, and where level 0 is, in words. */
    private String where() {
        final boolean near = staging.order() instanceof Order.BaseNear;
        if (staging.stance() instanceof Stance.Lying) {
            return "vertical layout, level 0 " + (near ? "on top" : "at the bottom");
        }
        return "horizontal layout, level 0 " + (near ? "in front" : "at the back");
    }
}
