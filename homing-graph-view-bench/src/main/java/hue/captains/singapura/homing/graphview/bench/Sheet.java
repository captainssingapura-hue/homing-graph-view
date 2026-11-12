package hue.captains.singapura.homing.graphview.bench;

import hue.captains.singapura.tao.ontology.Mutable;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * The sheet a drawing lies on, seen through a window: scroll to zoom toward the cursor, drag to pan, and F or a double
 * click to fit the whole drawing. A new drawing is fitted when it is shown.
 */
final class Sheet implements Mutable {

    private static final double MARGIN = 40;
    private static final double MOST = 4;
    private static final double LEAST = 0.01;
    private static final double ZOOM_RATE = 0.0025;

    private final Pane view = new Pane();
    private final Group content = new Group();
    private final Translate shift = new Translate();
    private final Scale scale = new Scale(1, 1, 0, 0);
    private double lastX;
    private double lastY;
    private boolean fitted;

    /** An empty sheet. */
    Sheet() {
        view.setBackground(Background.fill(new Colours().sheet()));
        content.getTransforms().setAll(shift, scale);
        view.getChildren().add(content);
        final Rectangle clip = new Rectangle();
        clip.widthProperty().bind(view.widthProperty());
        clip.heightProperty().bind(view.heightProperty());
        view.setClip(clip);
        view.setFocusTraversable(true);
        view.widthProperty().addListener((property, before, after) -> fitOnce());
        view.heightProperty().addListener((property, before, after) -> fitOnce());
        view.setOnScroll(this::zoom);
        view.setOnMousePressed(this::press);
        view.setOnMouseDragged(this::drag);
        view.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                fit();
            }
        });
        view.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F) {
                fit();
            }
        });
    }

    /** The sheet's node. */
    Pane node() {
        return view;
    }

    /** Lays the drawing on the sheet in place of the last, fitted to the window. */
    void show(final Drawing drawing) {
        content.getChildren().setAll(drawing.node());
        fitted = false;
        fitOnce();
    }

    /** Fits the whole drawing in the window, no larger than its own size. */
    void fit() {
        if (content.getChildren().isEmpty() || view.getWidth() <= 0 || view.getHeight() <= 0) {
            return;
        }
        final Bounds drawn = content.getChildren().getFirst().getBoundsInParent();
        final double zoom = Math.min(1, Math.min((view.getWidth() - 2 * MARGIN) / drawn.getWidth(),
                (view.getHeight() - 2 * MARGIN) / drawn.getHeight()));
        scale.setX(zoom);
        scale.setY(zoom);
        shift.setX(view.getWidth() / 2 - zoom * drawn.getCenterX());
        shift.setY(view.getHeight() / 2 - zoom * drawn.getCenterY());
        fitted = true;
    }

    private void fitOnce() {
        if (!fitted) {
            fit();
        }
    }

    /** Zooms toward the cursor: the point under it stays under it. */
    private void zoom(final ScrollEvent event) {
        final double before = scale.getX();
        final double after = Math.clamp(before * Math.exp(event.getDeltaY() * ZOOM_RATE), LEAST, MOST);
        final double x = (event.getX() - shift.getX()) / before;
        final double y = (event.getY() - shift.getY()) / before;
        scale.setX(after);
        scale.setY(after);
        shift.setX(event.getX() - after * x);
        shift.setY(event.getY() - after * y);
        event.consume();
    }

    private void press(final MouseEvent event) {
        lastX = event.getX();
        lastY = event.getY();
        view.requestFocus();
    }

    private void drag(final MouseEvent event) {
        shift.setX(shift.getX() + event.getX() - lastX);
        shift.setY(shift.getY() + event.getY() - lastY);
        lastX = event.getX();
        lastY = event.getY();
    }
}
