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
 * The mouse and keys of a view. The camera's take theirs from the navigator, by mode; selecting is the view's: click a
 * slate to select it, and press Esc to clear. A click on empty space clears too while orbiting, but not while flying,
 * where it is too easily a slip while looking round. Keys act only while the view has focus, which a click gives.
 */
final class Gestures implements Mutable {

    private final GraphView view;
    private final Navigator navigator;
    private final SubScene scene;
    private double lastX;
    private double lastY;

    /** The gestures of a view, over the scene it draws in and the navigator its camera follows. */
    Gestures(final GraphView view, final Navigator navigator, final SubScene scene) {
        this.view = view;
        this.navigator = navigator;
        this.scene = scene;
    }

    /** Starts listening. */
    void attach() {
        scene.setOnMousePressed(this::pressed);
        scene.setOnMouseDragged(this::dragged);
        scene.setOnScroll(this::scrolled);
        scene.setOnMouseClicked(this::clicked);
        view.addEventFilter(KeyEvent.KEY_PRESSED, this::tab);
        view.setOnKeyPressed(this::key);
        view.setOnKeyReleased(event -> navigator.release(event.getCode()));
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
        navigator.drag(right, down, event.isSecondaryButtonDown() || event.isMiddleButtonDown() || event.isShiftDown(),
                scene.getHeight());
    }

    private void scrolled(final ScrollEvent event) {
        navigator.scroll(event.getDeltaY(), event.getX(), event.getY(), scene.getWidth(), scene.getHeight());
    }

    private void clicked(final MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
            final Optional<Vertex> vertex = vertexAt(event.getPickResult().getIntersectedNode());
            if (vertex.isPresent()) {
                view.select(vertex.get());
            } else if (!navigator.free()) {
                view.clear();
            }
        }
    }

    /** Tab steps through the camera's modes, rather than moving the focus on. */
    private void tab(final KeyEvent event) {
        if (event.getCode() == KeyCode.TAB) {
            navigator.press(KeyCode.TAB);
            event.consume();
        }
    }

    private void key(final KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            view.clear();
            event.consume();
        } else if (navigator.press(event.getCode())) {
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
