package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Scalar;
import java.util.Objects;

/**
 * How a graph is staged in 3D: how each layer's block is set, which end of the stack level 0 goes at, and what each
 * vertex is drawn as.
 *
 * @param stance standing or lying
 * @param order  level 0 at the far end of the stack or the near end
 * @param shapes what each vertex is drawn as, and the room every figure fits in
 */
public record Staging(Stance stance, Order order, Shapes shapes) implements Scalar {

    /** Refuses a missing part. */
    public Staging {
        Objects.requireNonNull(stance, "A staging needs its stance");
        Objects.requireNonNull(order, "A staging needs its order");
        Objects.requireNonNull(shapes, "A staging needs its shapes");
    }

    /** The default: standing, level 0 at the far end, every vertex in a full container. */
    public Staging() {
        this(new Stance.Standing(), new Order.BaseFar(), new ContainerShapes(new Uniform(new Container.Full())));
    }

    /** This staging with another stance. */
    public Staging with(final Stance other) {
        return new Staging(other, order, shapes);
    }

    /** This staging with another order. */
    public Staging with(final Order other) {
        return new Staging(stance, other, shapes);
    }

    /** This staging with other shapes. */
    public Staging with(final Shapes other) {
        return new Staging(stance, order, other);
    }
}
