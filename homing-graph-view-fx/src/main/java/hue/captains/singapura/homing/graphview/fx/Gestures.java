package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.Mutable;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import java.util.Optional;

/**
 * The mouse and keys of a view: drag to turn, drag with the right button or Shift to pan, scroll to zoom toward the
 * cursor; click a slate to select it, click empty space or press Esc to clear, and press H to frame the whole graph.
 */
final class Gestures implements Mutable {

    private static final double DEGREES_PER_PIXEL = 0.3;
    private static final double ZOOM_PER_SCROLL = 0.0025;

    private final GraphView view;
    private final CameraRig rig;
    private final SubScene scene;
    private double lastX;
    private double lastY;

    /** The gestures of a view, over the scene it draws in and the rig its camera is on. */
    Gestures(final GraphView view, final CameraRig rig, final SubScene scene) {
        this.view = view;
        this.rig = rig;
        this.scene = scene;
    }

    /** Starts listening. */
    void attach() {
        scene.setOnMousePressed(this::pressed);
        scene.setOnMouseDragged(this::dragged);
        scene.setOnScroll(this::scrolled);
        scene.setOnMouseClicked(this::clicked);
        view.setOnKeyPressed(this::key);
    }

    private void pressed(final MouseEvent event) {
        lastX = event.getX();
        lastY = event.getY();
        view.requestFocus();
    }

    private void dragged(final MouseEvent event) {
        final double right = event.getX() - lastX;
        final double down = event.getY() - lastY;
        lastX = event.getX();
        lastY = event.getY();
        if (event.isSecondaryButtonDown() || event.isMiddleButtonDown() || event.isShiftDown()) {
            final double units = rig.unitsPerPixel(scene.getHeight());
            rig.show(rig.orbit().panned(right * units, down * units));
        } else {
            rig.show(rig.orbit().turned(right * DEGREES_PER_PIXEL, -down * DEGREES_PER_PIXEL));
        }
    }

    private void scrolled(final ScrollEvent event) {
        final double units = rig.unitsPerPixel(scene.getHeight());
        final double factor = Math.exp(-event.getDeltaY() * ZOOM_PER_SCROLL);
        rig.show(rig.orbit().zoomed(factor, (event.getX() - scene.getWidth() / 2) * units,
                (event.getY() - scene.getHeight() / 2) * units));
    }

    private void clicked(final MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
            vertexAt(event.getPickResult().getIntersectedNode()).ifPresentOrElse(view::select, view::clear);
        }
    }

    private void key(final KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            view.clear();
            event.consume();
        } else if (event.getCode() == KeyCode.H || event.getCode() == KeyCode.HOME) {
            view.home();
            event.consume();
        }
    }

    /** The vertex of the slate a node belongs to, if it belongs to one. */
    private Optional<Vertex> vertexAt(final Node picked) {
        for (Node node = picked; node != null; node = node.getParent()) {
            if (node.getUserData() instanceof Vertex vertex) {
                return Optional.of(vertex);
            }
        }
        return Optional.empty();
    }
}
