package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.homing.graphview.fx.GraphView;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.tao.ontology.Mutable;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The demo: a window over GraphView showing a JDK module and every module it requires, java.se unless another is
 * named as the first argument, or ALL-SYSTEM for every module. The view's own readout says what the keys do.
 */
public final class Demo extends Application implements Mutable {

    /** Starts the demo. */
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) {
        final String root = getParameters().getRaw().stream().findFirst().orElse("java.se");
        final Graph graph = new JdkModules().of(root);
        final GraphView view = new GraphView(graph);
        stage.setTitle("homing-graph-view demo: " + root + " and what it requires - "
                + graph.vertices().size() + " modules, " + graph.edges().size() + " requires");
        stage.setScene(new Scene(view, 1280, 820));
        stage.show();
        view.requestFocus();
    }
}
