package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.homing.graphview.fx.Container;
import hue.captains.singapura.homing.graphview.fx.GraphView;
import hue.captains.singapura.homing.graphview.fx.Order;
import hue.captains.singapura.homing.graphview.fx.Stance;
import hue.captains.singapura.homing.graphview.fx.Staging;
import hue.captains.singapura.homing.graphview.fx.Uniform;
import hue.captains.singapura.homing.graphview.layout.Reachability;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.samples.JdkModules;
import hue.captains.singapura.homing.graphview.samples.RoyalFamily;
import hue.captains.singapura.tao.ontology.Mutable;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.List;

/**
 * The demo: a window over GraphView showing a JDK module and every module it requires, java.se unless another is
 * named as the first argument, or ALL-SYSTEM for every module, each module a container sized by its packages; or, for
 * royal, the British Royal Family, its generations as the layers, each person in a full container. The layers stand
 * unless an argument says lying, and level 0 is at the far end of the stack unless one says near: at the front when
 * standing, on top when lying. The view's own readout says what the keys do.
 */
public final class Demo extends Application implements Mutable {

    /** The first argument that asks for the royal family rather than a JDK module. */
    private static final String ROYAL = "royal";

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
        final boolean royal = ROYAL.equalsIgnoreCase(root);
        final Graph graph = royal ? new RoyalFamily().of() : new JdkModules().of(root);
        final Staging staging = new Staging(lying ? new Stance.Lying() : new Stance.Standing(),
                near ? new Order.BaseNear() : new Order.BaseFar(),
                royal ? new Uniform(new Container.Full()) : new PackageCounts());
        final GraphView view = new GraphView(graph, new Reachability(), staging);
        stage.setTitle("homing-graph-view demo: " + (royal ? "the British Royal Family - " + graph.vertices().size()
                + " people, " : root + " and what it requires - " + graph.vertices().size() + " modules, "
                + graph.edges().size() + " requires, ") + "layers "
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
