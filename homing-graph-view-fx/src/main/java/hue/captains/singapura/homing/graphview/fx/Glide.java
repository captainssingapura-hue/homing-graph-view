package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Mutable;
import javafx.animation.AnimationTimer;
import java.util.function.Consumer;

/** A camera move from one orbit to another over a moment, eased in and out, turning the short way round. */
final class Glide extends AnimationTimer implements Mutable {

    private static final double SECONDS = 0.7;

    private final Consumer<Orbit> place;
    private Orbit from;
    private Orbit to;
    private long started = -1;

    /** A glide that puts the camera at each step by the given means. */
    Glide(final Consumer<Orbit> place) {
        this.place = place;
    }

    /** Starts gliding between two orbits, dropping any glide under way. */
    void between(final Orbit from, final Orbit to) {
        this.from = from;
        this.to = to;
        this.started = -1;
        start();
    }

    @Override
    public void handle(final long now) {
        if (started < 0) {
            started = now;
        }
        final double done = Math.min(1, (now - started) / 1e9 / SECONDS);
        place.accept(from.toward(to, done * done * (3 - 2 * done)));
        if (done >= 1) {
            stop();
        }
    }
}
