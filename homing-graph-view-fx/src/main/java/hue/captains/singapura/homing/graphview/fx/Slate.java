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
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * A vertex drawn: a box with a face in front and one behind, so it reads from either side. With no attributes, each face
 * shows the vertex's ID. The slate's node carries its vertex as user data, which is how a click finds it.
 */
final class Slate implements Mutable {

    /** The box's width, height and depth, in scene units. */
    static final double WIDTH = 190;
    static final double HEIGHT = 46;
    static final double DEPTH = 10;
    private static final double MARGIN = 10;
    private static final double LIFT = 1.5;

    private final Group node;
    private final PhongMaterial material = new PhongMaterial();
    private final Label front;
    private final Label back;

    /**
     * A slate for the vertex, centred on the point.
     *
     * @param vertex the vertex
     * @param centre where its centre is in the scene
     * @param font   the faces' font
     */
    Slate(final Vertex vertex, final Point3D centre, final Font font) {
        final Box box = new Box(WIDTH, HEIGHT, DEPTH);
        box.setMaterial(material);
        front = face(vertex.id(), font, -(DEPTH / 2 + LIFT), 0);
        back = face(vertex.id(), font, DEPTH / 2 + LIFT, 180);
        node = new Group(box, front, back);
        node.setUserData(vertex);
        node.getTransforms().setAll(new Translate(centre.getX(), centre.getY(), centre.getZ()));
    }

    /** The slate's node in the scene. */
    Group node() {
        return node;
    }

    /** Draws the slate in a state. */
    void paint(final NodeState state, final Palette palette) {
        material.setDiffuseColor(palette.slate(state));
        material.setSpecularColor(palette.slate(state).brighter());
        front.setTextFill(palette.text(state));
        back.setTextFill(palette.text(state));
    }

    /** A face just off the box, turned to look out of its side. */
    private Label face(final String text, final Font font, final double z, final double turn) {
        final Label label = new Label(text);
        final double width = WIDTH - 2 * MARGIN;
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
