package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.homing.graphview.fx.Figure;
import hue.captains.singapura.homing.graphview.fx.Room;
import hue.captains.singapura.homing.graphview.fx.Shapes;
import hue.captains.singapura.homing.graphview.model.Vertex;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import java.util.List;

/**
 * The royal family's figures: each person a gilt photo frame holding the placeholder picture for their title, with
 * their name on a plaque along the bottom of the frame. The frame is what a selection colours.
 */
record Portraits() implements Shapes {

    private static final double WIDTH = 140;
    private static final double HEIGHT = 172;
    private static final double DEPTH = 14;
    private static final double BORDER = 10;
    private static final double PLAQUE = 28;
    private static final double BOARD = 4;
    private static final double LIFT = 0.6;
    private static final double TYPE = 12;
    private static final double SMALLEST_TYPE = 8;

    @Override
    public Room room() {
        return new Room(WIDTH, HEIGHT, DEPTH);
    }

    @Override
    public Figure of(final Vertex vertex) {
        final PhongMaterial gilt = new PhongMaterial(Color.web("#b8902e"));
        gilt.setSpecularColor(Color.web("#fff2c0"));
        final double opening = HEIGHT - BORDER - PLAQUE;
        final double middle = (BORDER - PLAQUE) / 2;
        final Box top = bar(WIDTH, BORDER, 0, -(HEIGHT - BORDER) / 2, gilt);
        final Box plaque = bar(WIDTH, PLAQUE, 0, (HEIGHT - PLAQUE) / 2, gilt);
        final Box left = bar(BORDER, opening, -(WIDTH - BORDER) / 2, middle, gilt);
        final Box right = bar(BORDER, opening, (WIDTH - BORDER) / 2, middle, gilt);
        final Box board = new Box(WIDTH - 2 * BORDER, opening, BOARD);
        board.setMaterial(new PhongMaterial(Color.web("#2a241c")));
        board.setTranslateY(middle);
        board.setTranslateZ(DEPTH / 2 - BOARD / 2);
        final ImageView picture = new ImageView(new Placeholders().of(new Titles().of(vertex.id())));
        picture.setFitWidth(WIDTH - 2 * BORDER);
        picture.setFitHeight(opening);
        picture.getTransforms().setAll(new Translate(-(WIDTH - 2 * BORDER) / 2, middle - opening / 2,
                DEPTH / 2 - BOARD - LIFT));
        return new Figure(new Group(board, picture, top, plaque, left, right, name(vertex.id())),
                List.of(top, plaque, left, right));
    }

    /** A bar of the frame. */
    private Box bar(final double width, final double height, final double x, final double y,
                    final PhongMaterial gilt) {
        final Box bar = new Box(width, height, DEPTH);
        bar.setMaterial(gilt);
        bar.setTranslateX(x);
        bar.setTranslateY(y);
        return bar;
    }

    /** The name on the plaque, in smaller type if it is long. */
    private Label name(final String text) {
        final double width = WIDTH - 2 * BORDER;
        final Label label = new Label(text);
        final Text probe = new Text(text);
        probe.setFont(Font.font(TYPE));
        final double needed = probe.getLayoutBounds().getWidth();
        label.setFont(Font.font(needed <= width ? TYPE : Math.max(SMALLEST_TYPE, TYPE * width / needed)));
        label.setTextFill(Color.web("#2a1f0f"));
        label.setAlignment(Pos.CENTER);
        label.setMinSize(width, PLAQUE);
        label.setPrefSize(width, PLAQUE);
        label.setMaxSize(width, PLAQUE);
        label.setMouseTransparent(true);
        label.getTransforms().setAll(new Translate(-width / 2, HEIGHT / 2 - PLAQUE, -DEPTH / 2 - LIFT));
        return label;
    }
}
