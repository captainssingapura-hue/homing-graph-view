package hue.captains.singapura.homing.graphview.viewer;

import hue.captains.singapura.tao.ontology.Mutable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * The viewer's details panel, down the right: the explanation of the vertex selected, or, with nothing selected, how
 * to select one. It knows nothing of the view: the viewer tells it what to show as the view's selection events come in.
 */
final class Details implements Mutable {

    private static final double WIDTH = 260;
    private static final double SPACING = 8;
    private static final double PADDING = 10;

    private final VBox node;
    private final Label heading = new Label();
    private final Label body = new Label();

    /** The panel, with nothing selected. */
    Details() {
        final Label title = new Label("Details");
        title.setStyle("-fx-text-fill: #9aa3b2; -fx-font-weight: bold;");
        heading.setStyle("-fx-text-fill: #eef2f7; -fx-font-size: 16px; -fx-font-weight: bold;");
        body.setStyle("-fx-text-fill: #c9d1dc; -fx-font-size: 12px;");
        heading.setWrapText(true);
        body.setWrapText(true);
        node = new VBox(SPACING, title, heading, body);
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

    /** Shows the explanation of a vertex selected. */
    void show(final Explanation explanation) {
        heading.setText(explanation.heading());
        body.setText(explanation.body());
    }

    /** Shows that nothing is selected, and how to select something. */
    void clear() {
        heading.setText("Nothing selected");
        body.setText("Click a figure to see what it is. Esc clears the selection.");
    }
}
