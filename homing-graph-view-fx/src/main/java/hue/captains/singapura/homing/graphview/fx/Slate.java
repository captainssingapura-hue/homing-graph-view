package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.Mutable;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * A vertex drawn as its container: a box as long as its container's size says, with a face in front and one behind, so
 * it reads from either side. With no attributes, each face shows the vertex's ID, in smaller type if the face is too
 * short for it. The node carries its vertex as user data, which is how a click finds it.
 */
final class Slate implements Mutable {

    /** A full container's length, and every container's height and depth, in scene units. */
    static final double LENGTH = 190;
    static final double HEIGHT = 46;
    static final double DEPTH = 44;
    private static final double MARGIN = 8;
    private static final double LIFT = 1.5;
    private static final double SMALLEST_TYPE = 9;

    private final Group node;
    private final PhongMaterial material = new PhongMaterial();
    private final Label front;
    private final Label back;

    /**
     * A container for the vertex, centred on the point.
     *
     * @param vertex    the vertex
     * @param centre    where its centre is in the scene
     * @param font      the faces' font, made smaller if a face is too short for the ID
     * @param container its size
     */
    Slate(final Vertex vertex, final Point3D centre, final Font font, final Container container) {
        final double length = LENGTH * container.length();
        final Box box = new Box(length, HEIGHT, DEPTH);
        box.setMaterial(material);
        final double width = length - 2 * MARGIN;
        final Font fitted = fitted(vertex.id(), font, width);
        front = face(vertex.id(), fitted, width, -(DEPTH / 2 + LIFT), 0);
        back = face(vertex.id(), fitted, width, DEPTH / 2 + LIFT, 180);
        node = new Group(box, front, back);
        node.setUserData(vertex);
        node.getTransforms().setAll(new Translate(centre.getX(), centre.getY(), centre.getZ()));
    }

    /** The container's node in the scene. */
    Group node() {
        return node;
    }

    /** Draws the container in a state. */
    void paint(final NodeState state, final Palette palette) {
        material.setDiffuseColor(palette.slate(state));
        material.setSpecularColor(palette.slate(state).brighter());
        front.setTextFill(palette.text(state));
        back.setTextFill(palette.text(state));
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
    private Label face(final String text, final Font font, final double width, final double z, final double turn) {
        final Label label = new Label(text);
        label.setFont(font);
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
