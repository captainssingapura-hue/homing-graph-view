package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.tao.ontology.Mutable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * The demo's details panel, down the right: what is said of the vertex selected, or, with nothing selected, how to
 * select one. It knows nothing of the view: the demo tells it what to show as the view's selection events come in.
 */
final class Details implements Mutable {

    private static final double WIDTH = 260;
    private static final double SPACING = 8;
    private static final double PADDING = 10;

    private final VBox node;
    private final Label name = new Label();
    private final Label body = new Label();

    /** The panel, with nothing selected. */
    Details() {
        final Label heading = new Label("Details");
        heading.setStyle("-fx-text-fill: #9aa3b2; -fx-font-weight: bold;");
        name.setStyle("-fx-text-fill: #eef2f7; -fx-font-size: 16px; -fx-font-weight: bold;");
        body.setStyle("-fx-text-fill: #c9d1dc; -fx-font-size: 12px;");
        name.setWrapText(true);
        body.setWrapText(true);
        node = new VBox(SPACING, heading, name, body);
        node.setPadding(new Insets(PADDING));
        node.setPrefWidth(WIDTH);
        node.setMinWidth(WIDTH);
        node.setStyle("-fx-background-color: #14171c;");
        clear();
    }

    /** The panel's node. */
    VBox node() {
        return node;
    }

    /** Shows what is said of a vertex selected. */
    void show(final Detail detail) {
        name.setText(detail.heading());
        body.setText(detail.body());
    }

    /** Shows that nothing is selected, and how to select something. */
    void clear() {
        name.setText("Nothing selected");
        body.setText("Click a figure to see what it is. Esc clears the selection.");
    }
}
