package hue.captains.singapura.homing.graphview.cats;

import hue.captains.singapura.homing.graphview.fx.Figure;
import hue.captains.singapura.homing.graphview.fx.Room;
import hue.captains.singapura.homing.graphview.fx.Shapes;
import hue.captains.singapura.homing.graphview.model.Vertex;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import java.util.List;

/**
 * Each breed a cat's-head badge in the colour of its coat - ginger for long hair, grey for short, pink for none, amber
 * for a wild cat - with its name on a plate beneath. The head and ears are what a selection colours.
 */
record CatBadges() implements Shapes {

    private static final double WIDTH = 120;
    private static final double HEIGHT = 150;
    private static final double DEPTH = 14;
    private static final double HEAD = 46;
    private static final double HEAD_Y = -12;
    private static final double EAR = 28;
    private static final double PLATE = 26;

    @Override
    public Room room() {
        return new Room(WIDTH, HEIGHT, DEPTH);
    }

    @Override
    public Figure of(final Vertex vertex) {
        final Coat coat = new Studbook().breed(vertex.id()).map(Breed::coat).orElse(new Coat.Short());
        final PhongMaterial fur = new PhongMaterial(colour(coat));
        fur.setSpecularColor(colour(coat).brighter());
        final Cylinder head = new Cylinder(HEAD, DEPTH);
        head.setMaterial(fur);
        head.getTransforms().setAll(new Translate(0, HEAD_Y, 0), new Rotate(90, Rotate.X_AXIS));
        final Box left = ear(-1, fur);
        final Box right = ear(1, fur);
        final Group face = new Group(eye(-1), eye(1), nose());
        final Box plate = new Box(WIDTH, PLATE, DEPTH - 4);
        plate.setMaterial(new PhongMaterial(Color.web("#2b2f38")));
        plate.setTranslateY(HEIGHT / 2 - PLATE / 2);
        final List<Shape3D> tinted = List.of(head, left, right);
        return new Figure(new Group(left, right, head, face, plate, name(vertex.id())), tinted);
    }

    /** An ear: a square turned on its corner, half hidden behind the head. */
    private Box ear(final int side, final PhongMaterial fur) {
        final Box ear = new Box(EAR, EAR, DEPTH - 2);
        ear.setMaterial(fur);
        ear.getTransforms().setAll(new Translate(side * 30, HEAD_Y - 36, 1), new Rotate(45, Rotate.Z_AXIS));
        return ear;
    }

    private Sphere eye(final int side) {
        final Sphere eye = new Sphere(6);
        eye.setMaterial(new PhongMaterial(Color.web("#7cc35a")));
        eye.getTransforms().setAll(new Translate(side * 17, HEAD_Y - 8, -DEPTH / 2));
        return eye;
    }

    private Sphere nose() {
        final Sphere nose = new Sphere(4);
        nose.setMaterial(new PhongMaterial(Color.web("#e8848f")));
        nose.getTransforms().setAll(new Translate(0, HEAD_Y + 8, -DEPTH / 2));
        return nose;
    }

    /** The breed's name on the plate, in smaller type if it is long. */
    private Label name(final String text) {
        final Label label = new Label(text);
        label.setFont(Font.font(text.length() > 16 ? 10 : 12));
        label.setTextFill(Color.web("#eef2f7"));
        label.setAlignment(Pos.CENTER);
        label.setMinSize(WIDTH, PLATE);
        label.setPrefSize(WIDTH, PLATE);
        label.setMaxSize(WIDTH, PLATE);
        label.setMouseTransparent(true);
        label.getTransforms().setAll(new Translate(-WIDTH / 2, HEIGHT / 2 - PLATE, -(DEPTH - 4) / 2 - 0.6));
        return label;
    }

    private Color colour(final Coat coat) {
        return switch (coat) {
            case Coat.Long() -> Color.web("#c98b4a");
            case Coat.Short() -> Color.web("#8d96a3");
            case Coat.Hairless() -> Color.web("#e2b9a3");
            case Coat.Wild() -> Color.web("#b07a34");
        };
    }
}
