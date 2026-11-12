package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.tao.ontology.Mutable;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The demo: a window over GraphView with a list of demos to pick from - JDK modules, each a container sized by its
 * packages, and the British Royal Family, its generations as the layers. The arguments say which to start on (royal,
 * or a JDK module; java.se if none), and how to stage them all: lying rather than standing, and near to put level 0 at
 * the near end of the stack, in front when standing and on top when lying. The view's own readout says what the keys
 * do.
 */
public final class Demo extends Application implements Mutable {

    /** Starts the demo. */
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) {
        final Showcase showcase = new Showcase(getParameters().getRaw(), stage::setTitle);
        stage.setScene(new Scene(showcase.node(), 1280, 820));
        stage.show();
    }
}
