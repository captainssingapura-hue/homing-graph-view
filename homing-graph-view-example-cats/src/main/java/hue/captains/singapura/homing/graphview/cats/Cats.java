package hue.captains.singapura.homing.graphview.cats;

import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.viewer.Exhibit;
import hue.captains.singapura.homing.graphview.viewer.Registry;
import hue.captains.singapura.homing.graphview.viewer.ViewerApplication;
import java.util.List;

/**
 * Cat breeds and the breeds they were bred from, in the ready-made viewer: once as cat's-head badges explained from the
 * studbook, and once plainly, as the viewer draws any graph it is given nothing more for.
 */
public final class Cats extends ViewerApplication {

    /** Starts the example. */
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    protected Registry registry(final List<String> args) {
        final Graph breeds = new Studbook().graph();
        return new Registry(List.of(new Exhibit("Cat breeds", breeds, new CatBadges(), new CatExplanations()),
                new Exhibit("Cat breeds, plainly", breeds)));
    }
}
