package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Scalar;
import java.util.Objects;

/**
 * How a graph is staged in 3D: how each layer's block is set, which end of the stack level 0 goes at, and which
 * container each vertex comes in.
 *
 * @param stance     standing or lying
 * @param order      level 0 at the far end of the stack or the near end
 * @param containers which container each vertex comes in
 */
public record Staging(Stance stance, Order order, Containers containers) implements Scalar {

    /** Refuses a missing part. */
    public Staging {
        Objects.requireNonNull(stance, "A staging needs its stance");
        Objects.requireNonNull(order, "A staging needs its order");
        Objects.requireNonNull(containers, "A staging needs its containers");
    }

    /** The default: standing, level 0 at the far end, every vertex in a full container. */
    public Staging() {
        this(new Stance.Standing(), new Order.BaseFar(), new Uniform(new Container.Full()));
    }

    /** This staging with another stance. */
    public Staging with(final Stance other) {
        return new Staging(other, order, containers);
    }

    /** This staging with another order. */
    public Staging with(final Order other) {
        return new Staging(stance, other, containers);
    }

    /** This staging with other containers. */
    public Staging with(final Containers other) {
        return new Staging(stance, order, other);
    }
}
