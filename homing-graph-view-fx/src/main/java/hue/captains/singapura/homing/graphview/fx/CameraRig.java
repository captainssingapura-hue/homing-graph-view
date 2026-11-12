package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Mutable;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * The camera and the light it carries, placed by an orbit: moved to the target, turned by the yaw and the pitch, and
 * backed off by the distance. It can jump to an orbit or glide there, or stand where a free camera is.
 */
final class CameraRig implements Mutable {

    private static final double FIELD_OF_VIEW = 35;
    private static final double FREE_NEAR = 5;
    private static final double FREE_FAR = 60_000;

    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Group node;
    private final Translate target = new Translate();
    private final Rotate yaw = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate pitch = new Rotate(0, Rotate.X_AXIS);
    private final Translate back = new Translate();
    private final Glide glide = new Glide(this::place);
    private Orbit orbit = new Orbit(0, 0, 0, 0, 0, 1000);

    /** A rig at a default orbit until it is shown another. */
    CameraRig() {
        camera.setFieldOfView(FIELD_OF_VIEW);
        node = new Group(camera, new PointLight(Color.gray(0.55)));
        node.getTransforms().setAll(target, yaw, pitch, back);
        place(orbit);
    }

    /** The rig's node, for the scene. */
    Group node() {
        return node;
    }

    /** The camera, for the scene. */
    PerspectiveCamera camera() {
        return camera;
    }

    /** Where the camera is now. */
    Orbit orbit() {
        return orbit;
    }

    /** The camera's vertical field of view, in degrees. */
    double fieldOfView() {
        return FIELD_OF_VIEW;
    }

    /** Scene units per pixel at the target, for a view this many pixels high. */
    double unitsPerPixel(final double height) {
        return orbit.distance() * Math.tan(Math.toRadians(FIELD_OF_VIEW / 2)) / (Math.max(height, 1) / 2);
    }

    /** Jumps to an orbit, stopping any glide. */
    void show(final Orbit next) {
        glide.stop();
        place(next);
    }

    /** Glides from where the camera is to an orbit. */
    void glideTo(final Orbit next) {
        glide.between(orbit, next);
    }

    /** Puts the camera where a free camera is, turned in place rather than round a point, stopping any glide. */
    void free(final FlyCamera flying) {
        glide.stop();
        target.setX(flying.x());
        target.setY(flying.y());
        target.setZ(flying.z());
        yaw.setAngle(flying.yaw());
        pitch.setAngle(flying.pitch());
        back.setZ(0);
        camera.setNearClip(FREE_NEAR);
        camera.setFarClip(FREE_FAR);
    }

    private void place(final Orbit next) {
        orbit = next;
        target.setX(next.x());
        target.setY(next.y());
        target.setZ(next.z());
        yaw.setAngle(next.yaw());
        pitch.setAngle(next.pitch());
        back.setZ(-next.distance());
        camera.setNearClip(Math.max(1, next.distance() / 100));
        camera.setFarClip(next.distance() * 20);
    }
}
