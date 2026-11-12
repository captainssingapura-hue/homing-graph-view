package hue.captains.singapura.homing.graphview.viewer;

import hue.captains.singapura.homing.graphview.fx.Order;
import hue.captains.singapura.homing.graphview.fx.Stance;
import hue.captains.singapura.homing.graphview.fx.Staging;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import java.util.List;
import java.util.Optional;

/**
 * What a viewer's command-line arguments say, in any order: lying to start in the vertical layout, near to put level 0
 * at the near end of the stack, and any other word part of the name of the exhibit to start on.
 */
public record Arguments() implements StatelessFunctionalObject {

    private static final String LYING = "lying";
    private static final String NEAR = "near";

    /**
     * How to stage every exhibit: standing unless lying is asked for, level 0 at the far end unless near is.
     *
     * @param args the arguments
     */
    public Staging staging(final List<String> args) {
        return new Staging().with(args.contains(LYING) ? new Stance.Lying() : new Stance.Standing())
                .with(args.contains(NEAR) ? new Order.BaseNear() : new Order.BaseFar());
    }

    /**
     * The first argument that is not a word about staging: what to look for among the exhibits' names.
     *
     * @param args the arguments
     */
    public Optional<String> asked(final List<String> args) {
        return args.stream().filter(arg -> !staging(arg)).findFirst();
    }

    /**
     * Where the exhibit to start on is in the registry: the first whose name holds the word asked for, or the first of
     * all if none does or none is asked for.
     *
     * @param args     the arguments
     * @param registry the exhibits
     */
    public int first(final List<String> args, final Registry registry) {
        return asked(args).flatMap(registry::find).orElse(0);
    }

    private boolean staging(final String arg) {
        return List.of(LYING, NEAR, "standing", "far").contains(arg);
    }
}
