package hue.captains.singapura.homing.graphview.bench;

import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.Mutable;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

/**
 * A vertex drawn flat: a slate the 3D view's size, showing the vertex's ID, centred on its slot. It lights under the
 * mouse, and its tooltip gives the whole ID when the slate is too narrow for it.
 */
final class Card implements Mutable {

    /** The slate's width and height, as in the 3D view. */
    static final double WIDTH = 190;
    static final double HEIGHT = 46;
    private static final double MARGIN = 10;
    private static final double ROUNDING = 8;

    private final StackPane node;

    /**
     * A card for the vertex, centred on the point.
     *
     * @param vertex  the vertex
     * @param centre  its slot's centre on the sheet
     * @param font    the name's font
     * @param colours the colours it is drawn in
     */
    Card(final Vertex vertex, final Point2D centre, final Font font, final Colours colours) {
        final Rectangle face = new Rectangle(WIDTH, HEIGHT, colours.slate());
        face.setArcWidth(ROUNDING);
        face.setArcHeight(ROUNDING);
        final Label name = new Label(vertex.id());
        name.setFont(font);
        name.setTextFill(colours.text());
        name.setPrefWidth(WIDTH - 2 * MARGIN);
        name.setMaxWidth(WIDTH - 2 * MARGIN);
        name.setAlignment(Pos.CENTER);
        name.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        name.setMouseTransparent(true);
        node = new StackPane(face, name);
        node.setMaxSize(WIDTH, HEIGHT);
        node.resizeRelocate(centre.getX() - WIDTH / 2, centre.getY() - HEIGHT / 2, WIDTH, HEIGHT);
        node.setOnMouseEntered(event -> face.setFill(colours.slateHovered()));
        node.setOnMouseExited(event -> face.setFill(colours.slate()));
        Tooltip.install(node, new Tooltip(vertex.id()));
    }

    /** The card's node on the sheet. */
    StackPane node() {
        return node;
    }
}
