package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.model.Vertex;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import java.util.List;
import java.util.Objects;

/**
 * The default figures: each vertex a container, as long as its container's size says, with its ID on both long faces,
 * in smaller type if the face is too short for it. The box is what a selection colours.
 *
 * @param containers which container each vertex comes in
 */
public record ContainerShapes(Containers containers) implements Shapes {

    /** A full container's length, and every container's height and depth, in scene units. */
    private static final double LENGTH = 190;
    private static final double HEIGHT = 46;
    private static final double DEPTH = 44;
    private static final double MARGIN = 8;
    private static final double LIFT = 1.5;
    private static final double TYPE = 15;
    private static final double SMALLEST_TYPE = 9;

    /** Refuses missing containers. */
    public ContainerShapes {
        Objects.requireNonNull(containers, "Container shapes need to know which container each vertex comes in");
    }

    @Override
    public Room room() {
        return new Room(LENGTH, HEIGHT, DEPTH);
    }

    @Override
    public Figure of(final Vertex vertex) {
        final double length = LENGTH * containers.of(vertex).length();
        final Box box = new Box(length, HEIGHT, DEPTH);
        final Palette palette = new Palette();
        final PhongMaterial material = new PhongMaterial(palette.slate(new NodeState.Idle()));
        material.setSpecularColor(palette.slate(new NodeState.Idle()).brighter());
        box.setMaterial(material);
        final double width = length - 2 * MARGIN;
        final Font font = fitted(vertex.id(), Font.font(TYPE), width);
        final Color ink = palette.text(new NodeState.Idle());
        return new Figure(new Group(box, face(vertex.id(), font, ink, width, -(DEPTH / 2 + LIFT), 0),
                face(vertex.id(), font, ink, width, DEPTH / 2 + LIFT, 180)), List.of(box));
    }

    /** The font, made smaller, down to the smallest type, until the text fits the width. */
    private Font fitted(final String text, final Font font, final double width) {
        final Text probe = new Text(text);
        probe.setFont(font);
        final double needed = probe.getLayoutBounds().getWidth();
        return needed <= width ? font
                : Font.font(font.getFamily(), Math.max(SMALLEST_TYPE, font.getSize() * width / needed));
    }

    /** A face just off the box, turned to look out of its side. */
    private Label face(final String text, final Font font, final Color ink, final double width, final double z,
                       final double turn) {
        final Label label = new Label(text);
        label.setFont(font);
        label.setTextFill(ink);
        label.setAlignment(Pos.CENTER);
        label.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        label.setMinSize(width, HEIGHT);
        label.setPrefSize(width, HEIGHT);
        label.setMaxSize(width, HEIGHT);
        label.setMouseTransparent(true);
        label.getTransforms().setAll(new Translate(0, 0, z), new Rotate(turn, Rotate.Y_AXIS),
                new Translate(-width / 2, -HEIGHT / 2, 0));
        return label;
    }
}
