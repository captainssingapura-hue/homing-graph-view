package hue.captains.singapura.homing.graphview.viewer;

import hue.captains.singapura.tao.ontology.Mutable;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.List;

/**
 * A viewer as a JavaFX application: extend it, say which exhibits to show, and launch it. Its command-line arguments
 * are read as {@link Arguments} says: lying for the vertical layout, near for level 0 at the near end, and any other
 * word part of the name of the exhibit to start on.
 */
public abstract class ViewerApplication extends Application implements Mutable {

    private static final double WIDTH = 1400;
    private static final double HEIGHT = 820;

    /**
     * The exhibits to show.
     *
     * @param args the application's command-line arguments, for an application that reads more from them
     */
    protected abstract Registry registry(List<String> args);

    @Override
    public final void start(final Stage stage) {
        final List<String> args = getParameters().getRaw();
        final Registry registry = registry(args);
        final Arguments arguments = new Arguments();
        final Viewer viewer = new Viewer(registry, arguments.staging(args), arguments.first(args, registry),
                stage::setTitle);
        stage.setScene(new Scene(viewer.node(), WIDTH, HEIGHT));
        stage.show();
    }
}
