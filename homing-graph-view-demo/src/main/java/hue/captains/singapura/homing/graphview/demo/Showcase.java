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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The demo's window: a list of the demos down the left, and the one picked drawn beside it. Picking another draws it in
 * place of the last, staged the same way: standing unless the arguments say lying, level 0 at the far end unless they
 * say near. The first other argument picks the demo to start on: royal, or a JDK module, which joins the list if it is
 * not on it.
 */
final class Showcase implements Mutable {

    private static final String LYING = "lying";
    private static final String NEAR = "near";
    private final Set<String> words = Set.of(LYING, NEAR, "standing", "far");
    private static final double LIST_WIDTH = 220;
    private static final double SPACING = 6;
    private static final double PADDING = 10;

    private final BorderPane root = new BorderPane();
    private final ListView<String> list = new ListView<>();
    private final List<Exhibit> exhibits = new ArrayList<>(List.of(new Exhibit.Modules("java.se"),
            new Exhibit.Modules("java.desktop"), new Exhibit.Modules("jdk.jshell"), new Exhibit.Modules("ALL-SYSTEM"),
            new Exhibit.Royals()));
    private final Staging staging;
    private final Consumer<String> title;

    /**
     * The window's content, on the demo the arguments pick.
     *
     * @param args  the demo's arguments: lying, near, and royal or a JDK module, in any order
     * @param title where to say what is shown, such as the window's title
     */
    Showcase(final List<String> args, final Consumer<String> title) {
        this.title = title;
        final boolean lying = args.contains(LYING);
        final boolean near = args.contains(NEAR);
        this.staging = new Staging().with(lying ? new Stance.Lying() : new Stance.Standing())
                .with(near ? new Order.BaseNear() : new Order.BaseFar());
        final int first = start(args.stream().filter(arg -> !words.contains(arg)).findFirst().orElse("java.se"));
        list.getItems().setAll(exhibits.stream().map(Exhibit::name).toList());
        list.getSelectionModel().selectedIndexProperty().addListener((property, before, after) -> {
            if (after.intValue() >= 0) {
                show(exhibits.get(after.intValue()));
            }
        });
        final Label heading = new Label("Demos");
        heading.setStyle("-fx-text-fill: #9aa3b2; -fx-font-weight: bold;");
        list.setStyle("-fx-control-inner-background: #1a1e25; -fx-background-color: #1a1e25;");
        VBox.setVgrow(list, Priority.ALWAYS);
        final VBox side = new VBox(SPACING, heading, list);
        side.setPadding(new Insets(PADDING));
        side.setPrefWidth(LIST_WIDTH);
        side.setStyle("-fx-background-color: #14171c;");
        root.setLeft(side);
        list.getSelectionModel().select(first);
    }

    /** The window's content. */
    BorderPane node() {
        return root;
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

    /** Draws the demo in place of the last, or says why it cannot. */
    private void show(final Exhibit exhibit) {
        try {
            final Graph graph = exhibit.graph();
            final GraphView view = new GraphView(graph, new Reachability(), staging.with(exhibit.containers()));
            root.setCenter(view);
            title.accept("homing-graph-view demo: " + exhibit.title(graph) + ", layers " + where());
            view.requestFocus();
        } catch (final IllegalArgumentException refused) {
            final Label why = new Label(refused.getMessage());
            why.setStyle("-fx-text-fill: #e0a030; -fx-padding: 20;");
            root.setCenter(why);
            title.accept("homing-graph-view demo: " + refused.getMessage());
        }
    }

    /** How the layers are set, and where level 0 is, in words. */
    private String where() {
        final boolean near = staging.order() instanceof Order.BaseNear;
        if (staging.stance() instanceof Stance.Lying) {
            return "lying, level 0 " + (near ? "on top" : "at the bottom");
        }
        return "standing, level 0 " + (near ? "in front" : "at the back");
    }
}
