package hue.captains.singapura.homing.graphview.fx;

import javafx.geometry.Point3D;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The camera's maths: an orbit's axes, its moves and framing, without a scene. */
class OrbitTest {

    private static final double CLOSE = 1e-9;

    @Test
    void atYawAndPitchZeroTheCameraIsInFrontLookingAway() {
        final Orbit orbit = new Orbit(0, 0, 0, 0, 0, 100);
        assertClose(new Point3D(0, 0, -100), orbit.eye());
        assertClose(new Point3D(1, 0, 0), orbit.right());
        assertClose(new Point3D(0, 1, 0), orbit.down());
    }

    @Test
    void aNegativePitchLooksDownFromAbove() {
        final Orbit orbit = new Orbit(0, 0, 0, 0, -30, 100);
        assertTrue(orbit.eye().getY() < 0, "the camera is above: y points down");
        assertTrue(orbit.forward().getY() > 0, "and looks down");
    }

    @Test
    void theAxesStaySquareAtAnyAngle() {
        final Orbit orbit = new Orbit(5, -3, 2, 47, -31, 80);
        assertEquals(0, orbit.right().dotProduct(orbit.down()), CLOSE);
        assertEquals(0, orbit.right().dotProduct(orbit.forward()), CLOSE);
        assertEquals(0, orbit.down().dotProduct(orbit.forward()), CLOSE);
        assertEquals(80, orbit.eye().distance(orbit.target()), CLOSE);
    }

    @Test
    void theYawWrapsAndThePitchStopsShortOfThePoles() {
        assertEquals(-170, new Orbit(0, 0, 0, 190, 0, 1).yaw(), CLOSE);
        assertEquals(89, new Orbit(0, 0, 0, 0, 120, 1).pitch(), CLOSE);
        assertEquals(-89, new Orbit(0, 0, 0, 0, -120, 1).pitch(), CLOSE);
    }

    @Test
    void aDistanceThatIsNotPositiveIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new Orbit(0, 0, 0, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Orbit(0, 0, 0, 0, 0, Double.NaN));
    }

    @Test
    void panningMovesTheTargetAgainstTheDrag() {
        final Orbit panned = new Orbit(0, 0, 0, 0, 0, 100).panned(10, 0);
        assertClose(new Point3D(-10, 0, 0), panned.target());
    }

    @Test
    void zoomingKeepsThePointUnderTheCursorWhereItIs() {
        final Orbit orbit = new Orbit(0, 0, 0, 33, -20, 100);
        final Point3D cursor = orbit.target().add(orbit.right().multiply(30)).add(orbit.down().multiply(-12));
        final Orbit zoomed = orbit.zoomed(0.5, 30, -12);
        final Point3D offset = cursor.subtract(zoomed.target());
        assertEquals(15, offset.dotProduct(zoomed.right()), CLOSE);
        assertEquals(-6, offset.dotProduct(zoomed.down()), CLOSE);
        assertEquals(50, zoomed.distance(), CLOSE);
    }

    @Test
    void glidingTurnsTheShortWayRound() {
        final Orbit from = new Orbit(0, 0, 0, 170, 0, 100);
        final Orbit to = new Orbit(0, 0, 0, -170, 0, 100);
        assertEquals(180, Math.abs(from.toward(to, 0.5).yaw()), CLOSE);
        assertEquals(to, from.toward(to, 1));
    }

    @Test
    void framingCentresThePointsFromAnyAngleAndShowsThemAll() {
        final List<Point3D> points = List.of(new Point3D(-100, 0, 0), new Point3D(100, 0, 0),
                new Point3D(0, 50, 300));
        final double high = Math.tan(Math.toRadians(15));
        for (final double yaw : new double[] {0, 45, 135, -90}) {
            final Orbit framed = new Framing().of(points, 10, yaw, -20, 30, 1.5);
            assertClose(new Point3D(0, 25, 150), framed.target());
            double fullest = 0;
            for (final Point3D point : points) {
                final Point3D seen = point.subtract(framed.eye());
                final double ahead = seen.dotProduct(framed.forward());
                final double across = Math.abs(seen.dotProduct(framed.right())) / (high * 1.5);
                final double up = Math.abs(seen.dotProduct(framed.down())) / high;
                assertTrue(across <= ahead && up <= ahead, "a point falls outside the view at yaw " + yaw);
                fullest = Math.max(fullest, Math.max(across, up) / ahead);
            }
            assertTrue(fullest > 0.75, "the view is mostly filled at yaw " + yaw + ", not " + fullest);
        }
    }

    private void assertClose(final Point3D expected, final Point3D actual) {
        assertTrue(expected.distance(actual) < 1e-6, "expected " + expected + " but was " + actual);
    }
}
