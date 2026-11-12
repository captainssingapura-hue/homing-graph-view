package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.homing.graphview.fx.GraphView;
import hue.captains.singapura.homing.graphview.fx.Order;
import hue.captains.singapura.homing.graphview.fx.Stance;
import hue.captains.singapura.homing.graphview.fx.Staging;
import hue.captains.singapura.homing.graphview.layout.Reachability;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.samples.JdkModules;
import hue.captains.singapura.tao.ontology.Mutable;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.List;

/**
 * The demo: a window over GraphView showing a JDK module and every module it requires, java.se unless another is
 * named as the first argument, or ALL-SYSTEM for every module. Each module is a container sized by its packages. The
 * layers stand unless an argument says lying, and level 0 is at the far end of the stack unless one says near: at the
 * front when standing, on top when lying. The view's own readout says what the keys do.
 */
public final class Demo extends Application implements Mutable {

    /** Starts the demo. */
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) {
        final List<String> args = getParameters().getRaw();
        final String root = args.isEmpty() ? "java.se" : args.getFirst();
        final boolean lying = args.contains("lying");
        final boolean near = args.contains("near");
        final Graph graph = new JdkModules().of(root);
        final Staging staging = new Staging(lying ? new Stance.Lying() : new Stance.Standing(),
                near ? new Order.BaseNear() : new Order.BaseFar(), new PackageCounts());
        final GraphView view = new GraphView(graph, new Reachability(), staging);
        stage.setTitle("homing-graph-view demo: " + root + " and what it requires - "
                + graph.vertices().size() + " modules, " + graph.edges().size() + " requires, layers "
                + (lying ? "lying" : "standing") + ", level 0 " + where(lying, near));
        stage.setScene(new Scene(view, 1280, 820));
        stage.show();
        view.requestFocus();
    }

    /** Where level 0 is, in words. */
    private String where(final boolean lying, final boolean near) {
        if (lying) {
            return near ? "on top" : "at the bottom";
        }
        return near ? "in front" : "at the back";
    }
}
