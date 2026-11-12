package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.homing.graphview.fx.GraphView;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.tao.ontology.Mutable;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * The demo: a window over GraphView showing a JDK module and every module it requires, java.se unless another is
 * named as the first argument, or ALL-SYSTEM for every module.
 */
public final class Demo extends Application implements Mutable {

    private static final String HINT = "Drag to turn   ·   right-drag or Shift-drag to pan   ·   scroll to zoom"
            + "   ·   click a slate to select it   ·   Esc or a click on empty space to clear   ·   H for the whole graph";

    /** Starts the demo. */
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) {
        final String root = getParameters().getRaw().stream().findFirst().orElse("java.se");
        final Graph graph = new JdkModules().of(root);
        final GraphView view = new GraphView(graph);
        final Label hint = new Label(HINT);
        hint.setPadding(new Insets(6, 12, 6, 12));
        hint.setMaxWidth(Double.MAX_VALUE);
        hint.setStyle("-fx-background-color: #0d0f13; -fx-text-fill: #9aa3b2;");
        final BorderPane window = new BorderPane(view);
        window.setBottom(hint);
        stage.setTitle("homing-graph-view demo: " + root + " and what it requires - "
                + graph.vertices().size() + " modules, " + graph.edges().size() + " requires");
        stage.setScene(new Scene(window, 1280, 820));
        stage.show();
        view.requestFocus();
    }
}
