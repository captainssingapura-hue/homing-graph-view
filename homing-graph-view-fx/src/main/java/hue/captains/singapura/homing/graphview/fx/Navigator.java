package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Mutable;
import javafx.scene.input.KeyCode;
import java.util.function.Supplier;

/**
 * The camera's three modes and what the mouse and keys do in each. Orbit turns around a point; fly and drone are free
 * cameras moved by the keys. Tab steps through orbit, fly and drone, and W, A, S or D starts whichever of fly and drone
 * was used last, from the current view. Going back to orbit turns around the point ahead, so nothing jumps.
 */
final class Navigator implements Mutable {

    private static final double DEGREES_PER_PIXEL = 0.3;
    private static final double ZOOM_PER_SCROLL = 0.0025;
    private static final double NEAREST_TURN = 300;
    private static final double TURN_WHEN_NEAR = 800;

    private final CameraRig rig;
    private final Grounds grounds;
    private final Supplier<Orbit> home;
    private final Runnable changed;
    private final FreeFlight flight;
    private boolean free;
    private boolean drone;

    /**
     * The modes of a rig over some grounds.
     *
     * @param rig     the camera
     * @param grounds where a free camera may go
     * @param home    the whole graph, framed from the starting angle
     * @param changed told when what a readout shows may have changed
     */
    Navigator(final CameraRig rig, final Grounds grounds, final Supplier<Orbit> home, final Runnable changed) {
        this.rig = rig;
        this.grounds = grounds;
        this.home = home;
        this.changed = changed;
        this.flight = new FreeFlight(grounds, rig::free, changed);
    }

    /** A drag: turns or pans the orbit, or looks round when free. */
    void drag(final double right, final double down, final boolean pan, final double height) {
        if (free) {
            flight.look(right, down);
        } else if (pan) {
            final double units = rig.unitsPerPixel(height);
            rig.show(rig.orbit().panned(right * units, down * units));
        } else {
            rig.show(rig.orbit().turned(right * DEGREES_PER_PIXEL, -down * DEGREES_PER_PIXEL));
        }
    }

    /** A turn of the wheel at a point of the view: zooms toward it, or changes speed when free. */
    void scroll(final double delta, final double x, final double y, final double width, final double height) {
        if (free) {
            if (delta != 0) {
                flight.speed(delta > 0);
            }
            return;
        }
        final double units = rig.unitsPerPixel(height);
        rig.show(rig.orbit().zoomed(Math.exp(-delta * ZOOM_PER_SCROLL), (x - width / 2) * units,
                (y - height / 2) * units));
    }

    /** A key pressed; whether it was one of the camera's. */
    boolean press(final KeyCode key) {
        switch (key) {
            case TAB -> next();
            case H, HOME -> home();
            case DIGIT6 -> toFree(grounds.entrance(), drone);
            case W, A, S, D, SPACE, CONTROL, SHIFT -> {
                if (!free && (key == KeyCode.W || key == KeyCode.A || key == KeyCode.S || key == KeyCode.D)) {
                    toFree(rig.orbit().freed(), drone);
                }
                flight.hold(key);
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    /** A key let go. */
    void release(final KeyCode key) {
        flight.release(key);
    }

    /** Back to orbiting, gliding to the whole graph. */
    void home() {
        toOrbit();
        rig.glideTo(home.get());
    }

    /** Whether the camera is free, flying or droning. */
    boolean free() {
        return free;
    }

    /** Two lines for a readout: the mode with what it shows, and what the keys do in it. */
    String readout() {
        final String select = "click a container to select it · Esc to clear";
        if (!free) {
            return "ORBIT  ·  drag to turn · right-drag or Shift-drag to pan · wheel to zoom · H: home\n"
                    + select + " · Tab or W: fly · 6: walk in";
        }
        final String speed = "speed " + Math.round(flight.cruise()) + "/s";
        if (flight.drone()) {
            return "DRONE  ·  altitude " + Math.round(flight.altitude() / 10) * 10 + "  ·  " + speed + "\n"
                    + "W A S D move level · Space climb · Ctrl descend · Shift faster · wheel: speed · drag to look · "
                    + select + " · Tab: orbit · H: home";
        }
        return "FLYING  ·  " + speed + "\n"
                + "W A S D move along the view · Space up · Ctrl down · Shift faster · wheel: speed · drag to look · "
                + select + " · Tab: drone · H: home";
    }

    /** Tab: orbit, then fly, then drone, and round again, keeping the view. */
    private void next() {
        if (!free) {
            toFree(rig.orbit().freed(), false);
        } else if (!flight.drone()) {
            drone = true;
            flight.droning(true);
        } else {
            toOrbit();
        }
    }

    private void toFree(final FlyCamera from, final boolean asDrone) {
        free = true;
        drone = asDrone;
        flight.start(from, asDrone);
    }

    private void toOrbit() {
        if (!free) {
            return;
        }
        flight.stop();
        free = false;
        final FlyCamera camera = flight.camera();
        final double ahead = home.get().target().subtract(camera.position()).dotProduct(camera.forward());
        rig.show(camera.orbiting(ahead > NEAREST_TURN ? ahead : TURN_WHEN_NEAR));
        changed.run();
    }
}
