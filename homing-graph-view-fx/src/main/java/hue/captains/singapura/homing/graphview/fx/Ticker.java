package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Mutable;
import javafx.animation.AnimationTimer;
import java.util.function.DoubleConsumer;

/** Calls back on every frame with the seconds since the last, never more than a tenth, so a stall does not jump. */
final class Ticker extends AnimationTimer implements Mutable {

    private static final double LONGEST = 0.1;

    private final DoubleConsumer frame;
    private long last = -1;

    /** A ticker that hands each frame's seconds to the given means. */
    Ticker(final DoubleConsumer frame) {
        this.frame = frame;
    }

    @Override
    public void start() {
        last = -1;
        super.start();
    }

    @Override
    public void handle(final long now) {
        final double seconds = last < 0 ? 0 : Math.min(LONGEST, (now - last) / 1e9);
        last = now;
        frame.accept(seconds);
    }
}
