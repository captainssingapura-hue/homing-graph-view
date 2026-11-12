package hue.captains.singapura.homing.graphview.fx;

import javafx.geometry.Point3D;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The free camera's maths, ported from launcher-demo's FlyCameraTest: its 9 tests, and the grounds it roams. */
class FlyCameraTest {

    private static final double CLOSE = 1e-9;

    @Test
    void switchingBetweenOrbitAndFlyKeepsTheViewExactly() {
        final Orbit orbit = new Orbit(100, -50, 300, 28, -22, 1500);
        final FlyCamera fly = orbit.freed();
        near(orbit.eye(), fly.position());
        near(orbit.forward(), fly.forward());

        final Orbit back = fly.orbiting(1500);
        near(orbit.target(), back.target());
        assertEquals(orbit.yaw(), back.yaw(), CLOSE);
        assertEquals(orbit.pitch(), back.pitch(), CLOSE);
        // any distance gives the same view: only the point turned around differs
        near(fly.position(), fly.orbiting(400).eye());
    }

    @Test
    void movingAheadFollowsTheViewUpOrDownIncluded() {
        final FlyCamera lookingDown = new FlyCamera(0, 0, 0, 0, -45);
        final FlyCamera moved = lookingDown.moved(100, 0, 0);
        near(lookingDown.forward().multiply(100), moved.position());
        assertTrue(moved.y() > 0, "looking down, ahead goes down (y is down)");
    }

    @Test
    void risingIsStraightUpWhateverTheTilt() {
        final FlyCamera tilted = new FlyCamera(0, 0, 0, 37, -60);
        near(new Point3D(0, -50, 0), tilted.moved(0, 0, 50).position());
    }

    @Test
    void sidewaysIsLevel() {
        final FlyCamera tilted = new FlyCamera(0, 0, 0, 37, -60);
        assertEquals(0, tilted.moved(0, 80, 0).y(), CLOSE);
        assertEquals(80, tilted.moved(0, 80, 0).position().magnitude(), CLOSE);
    }

    @Test
    void aDroneKeepsItsHeightWhateverItLooksAt() {
        for (final double pitch : new double[] {-80, -30, 0, 45}) {
            final FlyCamera drone = new FlyCamera(10, -400, 20, 37, pitch);
            final FlyCamera moved = drone.cruised(250, -60, 0);
            assertEquals(-400, moved.y(), CLOSE, "level at pitch " + pitch);
            // ahead is where it faces, along the ground, and it does not turn
            final Point3D level = new Point3D(drone.forward().getX(), 0, drone.forward().getZ()).normalize();
            near(drone.position().add(level.multiply(250)).add(drone.right().multiply(-60)), moved.position());
            assertEquals(drone.yaw(), moved.yaw(), CLOSE);
            assertEquals(drone.pitch(), moved.pitch(), CLOSE);
        }
    }

    @Test
    void aDroneChangesHeightOnlyWhenToldAndThenStraightUp() {
        final FlyCamera drone = new FlyCamera(10, -400, 20, 37, -60);
        final FlyCamera climbed = drone.cruised(0, 0, 120);
        near(drone.position().add(0, -120, 0), climbed.position());
        near(drone.forward(), climbed.forward());
    }

    @Test
    void lookingTurnsInPlaceAndStopsShortOfStraightUpOrDown() {
        final FlyCamera fly = new FlyCamera(5, 6, 7, 10, 0);
        final FlyCamera turned = fly.looked(30, 200);
        near(fly.position(), turned.position());
        assertEquals(40, turned.yaw(), CLOSE);
        assertEquals(89, turned.pitch(), CLOSE);
    }

    @Test
    void flyingStaysInsideItsBox() {
        final FlyCamera far = new FlyCamera(9000, -9000, 50, 0, 0);
        near(new Point3D(1000, -1000, 50),
                far.within(new Point3D(-1000, -1000, -1000), new Point3D(1000, 1000, 1000)).position());
    }

    @Test
    void speedEasesTowardTheKeysTheSameWayAtAnyFrameRate() {
        final Velocity still = new Velocity(0, 0, 0);
        final Velocity target = new Velocity(400, -200, 100);
        final Velocity once = still.toward(target, 0.1);
        Velocity inSteps = still;
        for (int i = 0; i < 10; i++) {
            inSteps = inSteps.toward(target, 0.01);
        }
        assertEquals(once.ahead(), inSteps.ahead(), CLOSE);
        assertEquals(once.rightward(), inSteps.rightward(), CLOSE);
        // partway after a tenth of a second, nearly there after half a second
        assertTrue(once.ahead() > 200 && once.ahead() < 400, "" + once.ahead());
        assertTrue(still.toward(target, 0.5).ahead() > 396);
        // and it settles back to still when the keys are let go
        assertTrue(target.toward(still, 1).isStill());
    }

    @Test
    void theGroundsLieBelowTheGraphAndARoamerStaysAboveThem() {
        final Grounds grounds = new Grounds(List.of(new Point3D(-300, 0, 0), new Point3D(300, -70, 900)), 23);
        assertEquals(93, grounds.floor(), CLOSE);
        assertTrue(grounds.roamHigh().getY() < grounds.floor(), "the camera stays above the floor");
        final FlyCamera sunk = new FlyCamera(0, 5000, 0, 0, 0).within(grounds.roamLow(), grounds.roamHigh());
        assertTrue(grounds.altitude(sunk.position()) > 0);
        final FlyCamera entrance = grounds.entrance();
        assertTrue(entrance.z() < 0 && entrance.forward().getZ() > 0, "the walk in starts in front, looking in");
    }

    private void near(final Point3D expected, final Point3D actual) {
        assertTrue(expected.distance(actual) < 1e-6, "expected " + expected + " but was " + actual);
    }
}
