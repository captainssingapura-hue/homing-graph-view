package hue.captains.singapura.homing.graphview.viewer;

import hue.captains.singapura.homing.graphview.fx.GraphView;
import hue.captains.singapura.homing.graphview.fx.Order;
import hue.captains.singapura.homing.graphview.fx.Stance;
import hue.captains.singapura.homing.graphview.fx.Staging;
import hue.captains.singapura.homing.graphview.fx.VertexSelection;
import hue.captains.singapura.homing.graphview.layout.Reachability;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.Mutable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A ready-made window's content over GraphView: down the left, the registry's exhibits and a choice of layout,
 * horizontal or vertical; in the middle, the exhibit picked, drawn in the layout chosen with its own shapes and its
 * own edge styler; down the right, the explanation of what is selected in it, kept up by listening to the view's
 * selection events. Picking another exhibit or layout draws it afresh in place of the last.
 */
public final class Viewer implements Mutable {

    private static final double SIDE_WIDTH = 220;
    private static final double SPACING = 6;
    private static final double PADDING = 10;
    private static final String HEADING = "-fx-text-fill: #9aa3b2; -fx-font-weight: bold;";

    private final Registry registry;
    private final Consumer<String> title;
    private final BorderPane root = new BorderPane();
    private final ListView<String> list = new ListView<>();
    private final RadioButton horizontal = new RadioButton("Horizontal");
    private final RadioButton vertical = new RadioButton("Vertical");
    private final Details details = new Details();
    private Staging staging;

    /**
     * A viewer of the exhibits.
     *
     * @param registry the exhibits to show
     * @param staging  how to stage them to begin with: the stance and the order; each exhibit brings its own shapes
     * @param first    where the exhibit to start on is in the registry
     * @param title    where to say what is shown, such as a window's title
     */
    public Viewer(final Registry registry, final Staging staging, final int first, final Consumer<String> title) {
        this.registry = Objects.requireNonNull(registry, "A viewer needs its registry of exhibits");
        this.staging = Objects.requireNonNull(staging, "A viewer needs to know how to stage its exhibits");
        this.title = Objects.requireNonNull(title, "A viewer needs somewhere to say what it shows");
        list.getItems().setAll(registry.items().stream().map(Exhibit::name).toList());
        list.getSelectionModel().selectedIndexProperty().addListener((property, before, after) -> redraw());
        final ToggleGroup layouts = new ToggleGroup();
        horizontal.setToggleGroup(layouts);
        vertical.setToggleGroup(layouts);
        layouts.selectToggle(staging.stance() instanceof Stance.Lying ? vertical : horizontal);
        layouts.selectedToggleProperty().addListener((property, before, after) -> {
            this.staging = this.staging.with(after == vertical ? new Stance.Lying() : new Stance.Standing());
            redraw();
        });
        root.setLeft(side());
        root.setRight(details.node());
        list.getSelectionModel().select(Math.clamp(first, 0, registry.size() - 1));
    }

    /** The viewer's content, to put in a scene. */
    public BorderPane node() {
        return root;
    }

    /** The side panel: the list of exhibits, and under it the choice of layout. */
    private VBox side() {
        final Label exhibits = new Label("Exhibits");
        exhibits.setStyle(HEADING);
        list.setStyle("-fx-control-inner-background: #1a1e25; -fx-background-color: #1a1e25;");
        VBox.setVgrow(list, Priority.ALWAYS);
        final Label layout = new Label("Layout");
        layout.setStyle(HEADING);
        final Label how = new Label("Horizontal: the layers one behind another. Vertical: one above another.");
        how.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
        how.setWrapText(true);
        List.of(horizontal, vertical).forEach(button -> button.setStyle("-fx-text-fill: #c9d1dc;"));
        final VBox side = new VBox(SPACING, exhibits, list, layout, horizontal, vertical, how);
        VBox.setMargin(layout, new Insets(PADDING, 0, 0, 0));
        side.setPadding(new Insets(PADDING));
        side.setPrefWidth(SIDE_WIDTH);
        side.setStyle("-fx-background-color: #14171c;");
        return side;
    }

    /** Draws the exhibit picked, in the layout chosen, in place of the last. */
    private void redraw() {
        final int picked = list.getSelectionModel().getSelectedIndex();
        if (picked < 0) {
            return;
        }
        final Exhibit exhibit = registry.items().get(picked);
        final GraphView view = new GraphView(exhibit.graph(), new Reachability(), staging.with(exhibit.shapes()),
                exhibit.styler());
        details.clear();
        view.subscribe(event -> {
            switch (event) {
                case VertexSelection.Some(Vertex vertex) ->
                        details.show(exhibit.explanations().of(vertex, exhibit.graph()));
                case VertexSelection.Nothing() -> details.clear();
            }
        });
        root.setCenter(view);
        title.accept(exhibit.name() + " - " + exhibit.graph().vertices().size() + " vertices, "
                + exhibit.graph().edges().size() + " edges, " + where());
        view.requestFocus();
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
