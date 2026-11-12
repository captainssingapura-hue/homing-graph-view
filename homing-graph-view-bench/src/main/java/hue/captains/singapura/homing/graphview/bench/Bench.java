package hue.captains.singapura.homing.graphview.bench;

import hue.captains.singapura.tao.ontology.Mutable;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The bench: the layout drawn flat, in 2D, to look at and polish, apart from the 3D view. It starts on java.se unless
 * the first argument names another graph: a JDK module, ALL-SYSTEM, or layer sizes for a made-up graph.
 */
public final class Bench extends Application implements Mutable {

    /** Starts the bench. */
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) {
        final String first = String.join(" ", getParameters().getRaw());
        final Workbench bench = new Workbench(first.isBlank() ? "java.se" : first);
        stage.setTitle("homing-graph-view bench: the layout, flat");
        stage.setScene(new Scene(bench.node(), 1280, 820));
        stage.show();
    }
}
