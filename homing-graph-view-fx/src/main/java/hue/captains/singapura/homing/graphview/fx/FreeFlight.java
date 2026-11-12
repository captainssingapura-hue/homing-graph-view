package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Mutable;
import javafx.scene.input.KeyCode;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Flying and droning: a free camera moved by the keys held, eased to the speed they ask for, and kept inside the
 * grounds' roaming box. Flying moves along the view, up or down included; a drone moves level along its heading, so only
 * Space and Ctrl change its height, whatever it looks at.
 */
final class FreeFlight implements Mutable {

    private static final double LOOK_PER_PIXEL = 0.15;
    private static final double SHIFTED = 4;
    private static final double SPEED_STEP = 1.25;
    private static final double SLOWEST = 40;
    private static final double FASTEST = 6000;

    private final Grounds grounds;
    private final Consumer<FlyCamera> place;
    private final Runnable changed;
    private final Set<KeyCode> held = EnumSet.noneOf(KeyCode.class);
    private final Ticker ticker = new Ticker(this::step);
    private FlyCamera camera = new FlyCamera(0, 0, 0, 0, 0);
    private Velocity velocity = new Velocity(0, 0, 0);
    private double cruise = 360;
    private boolean drone;

    /**
     * Free flight over the grounds.
     *
     * @param grounds where the camera may go
     * @param place   puts the camera where it has flown to
     * @param changed told when what a readout shows may have changed
     */
    FreeFlight(final Grounds grounds, final Consumer<FlyCamera> place, final Runnable changed) {
        this.grounds = grounds;
        this.place = place;
        this.changed = changed;
    }

    /** Starts flying, or droning, from a camera: normally the current view, so nothing jumps. */
    void start(final FlyCamera from, final boolean asDrone) {
        drone = asDrone;
        camera = from.within(grounds.roamLow(), grounds.roamHigh());
        velocity = new Velocity(0, 0, 0);
        place.accept(camera);
        ticker.start();
        changed.run();
    }

    /** Stops, letting go of every key. */
    void stop() {
        ticker.stop();
        held.clear();
        velocity = new Velocity(0, 0, 0);
    }

    /** Switches between flying and droning without moving: they share the camera. */
    void droning(final boolean asDrone) {
        drone = asDrone;
        changed.run();
    }

    /** A key is held down: W, A, S, D, Space, Ctrl or Shift. */
    void hold(final KeyCode key) {
        held.add(key);
    }

    /** A key is let go. */
    void release(final KeyCode key) {
        held.remove(key);
    }

    /** Looks round by a drag: right looks right and up looks up. */
    void look(final double right, final double down) {
        camera = camera.looked(right * LOOK_PER_PIXEL, -down * LOOK_PER_PIXEL);
        place.accept(camera);
    }

    /** A notch of the wheel: faster away from the user, slower toward. */
    void speed(final boolean faster) {
        cruise = Math.max(SLOWEST, Math.min(FASTEST, faster ? cruise * SPEED_STEP : cruise / SPEED_STEP));
        changed.run();
    }

    /** Where the camera is. */
    FlyCamera camera() {
        return camera;
    }

    /** Whether it moves as a drone. */
    boolean drone() {
        return drone;
    }

    /** Its cruising speed, in scene units per second, before Shift. */
    double cruise() {
        return cruise;
    }

    /** How high it is above the floor. */
    double altitude() {
        return grounds.altitude(camera.position());
    }

    /** One frame: ease toward the speed the keys ask for, then move. */
    private void step(final double seconds) {
        final double speed = cruise * (held.contains(KeyCode.SHIFT) ? SHIFTED : 1);
        final Velocity wanted = new Velocity(speed * (axis(KeyCode.W) - axis(KeyCode.S)),
                speed * (axis(KeyCode.D) - axis(KeyCode.A)), speed * (axis(KeyCode.SPACE) - axis(KeyCode.CONTROL)));
        velocity = velocity.toward(wanted, seconds);
        if (!velocity.isStill()) {
            final double ahead = velocity.ahead() * seconds;
            final double rightward = velocity.rightward() * seconds;
            final double upward = velocity.upward() * seconds;
            camera = (drone ? camera.cruised(ahead, rightward, upward) : camera.moved(ahead, rightward, upward))
                    .within(grounds.roamLow(), grounds.roamHigh());
            place.accept(camera);
            changed.run();
        }
    }

    private int axis(final KeyCode key) {
        return held.contains(key) ? 1 : 0;
    }
}
