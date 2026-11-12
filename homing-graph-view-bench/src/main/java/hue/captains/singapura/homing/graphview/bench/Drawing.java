package hue.captains.singapura.homing.graphview.bench;

import hue.captains.singapura.homing.graphview.layout.Arrangement;
import hue.captains.singapura.homing.graphview.layout.Block;
import hue.captains.singapura.homing.graphview.layout.Seat;
import hue.captains.singapura.tao.ontology.Mutable;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

/**
 * One layer's arrangement drawn flat: its block, every slot of the block outlined, and a card in each slot a vertex
 * sits in. The block is centred on the sheet's origin; a slot is 230 by 80, a card and the room around it.
 */
final class Drawing implements Mutable {

    /** A slot's width and height. */
    static final double SLOT_WIDTH = 230;
    static final double SLOT_HEIGHT = 80;
    private static final double PADDING = 20;
    private static final double ROUNDING = 16;
    private static final double SLOT_ROUNDING = 8;
    private static final double DASH = 6;
    private static final double FONT_SIZE = 15;

    private final Group node = new Group();
    private final Colours colours = new Colours();
    private final Block block;

    /**
     * The drawing of a layer's arrangement.
     *
     * @param arrangement where the layer's vertices sit
     */
    Drawing(final Arrangement arrangement) {
        this.block = arrangement.block();
        final double width = block.columns() * SLOT_WIDTH;
        final double height = block.rows() * SLOT_HEIGHT;
        final Rectangle face = new Rectangle(-width / 2 - PADDING, -height / 2 - PADDING, width + 2 * PADDING,
                height + 2 * PADDING);
        face.setArcWidth(ROUNDING);
        face.setArcHeight(ROUNDING);
        face.setFill(colours.block());
        face.setStroke(colours.blockEdge());
        face.setMouseTransparent(true);
        node.getChildren().add(face);
        final double edge = (block.columns() - 1) / 2.0;
        for (int column = 0; column < block.columns(); column++) {
            for (int row = 0; row < block.rows(); row++) {
                node.getChildren().add(slot(centre(column - edge, row)));
            }
        }
        final Font font = Font.font(FONT_SIZE);
        for (final Seat seat : arrangement.items()) {
            final Point2D centre = centre(seat.slot().lane(), seat.slot().row());
            node.getChildren().add(new Card(seat.vertex(), centre, font, colours).node());
        }
    }

    /** The drawing's node. */
    Group node() {
        return node;
    }

    /** A slot's centre: its lane across from the block's middle, its row up from the block's bottom. */
    private Point2D centre(final double lane, final int row) {
        return new Point2D(lane * SLOT_WIDTH, block.rows() * SLOT_HEIGHT / 2 - (row + 0.5) * SLOT_HEIGHT);
    }

    private Node slot(final Point2D centre) {
        final Rectangle outline = new Rectangle(centre.getX() - Card.WIDTH / 2, centre.getY() - Card.HEIGHT / 2,
                Card.WIDTH, Card.HEIGHT);
        outline.setArcWidth(SLOT_ROUNDING);
        outline.setArcHeight(SLOT_ROUNDING);
        outline.setFill(null);
        outline.setStroke(colours.slot());
        outline.getStrokeDashArray().setAll(DASH, DASH);
        outline.setMouseTransparent(true);
        return outline;
    }
}
