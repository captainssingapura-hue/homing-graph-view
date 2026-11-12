package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * A placeholder picture for each title, painted rather than photographed: a head and shoulders on a ground of the
 * title's colour, a king and a queen crowned, a prince in a coronet and a princess in a tiara, and the title written
 * under them. Someone with no title is a plain head and shoulders on grey.
 */
record Placeholders() implements StatelessFunctionalObject {

    /** The picture's width and height, in pixels. */
    static final double WIDTH = 240;
    static final double HEIGHT = 268;
    private static final double MIDDLE = WIDTH / 2;
    private static final double HEAD_Y = 114;

    /**
     * The picture for a title.
     *
     * @param title the title
     */
    Image of(final Title title) {
        final Canvas canvas = new Canvas(WIDTH, HEIGHT);
        final GraphicsContext pen = canvas.getGraphicsContext2D();
        ground(pen, title);
        person(pen, title instanceof Title.Queen || title instanceof Title.Princess);
        switch (title) {
            case Title.King() -> crown(pen);
            case Title.Queen() -> crown(pen);
            case Title.Prince() -> coronet(pen);
            case Title.Princess() -> tiara(pen);
            case Title.Untitled() -> { }
        }
        caption(pen, title);
        final SnapshotParameters clear = new SnapshotParameters();
        clear.setFill(Color.TRANSPARENT);
        return canvas.snapshot(clear, null);
    }

    /** The ground: the title's colour, lightest behind the head. */
    private void ground(final GraphicsContext pen, final Title title) {
        final Color colour = switch (title) {
            case Title.King() -> Color.web("#8c2233");
            case Title.Queen() -> Color.web("#2a4a9e");
            case Title.Prince() -> Color.web("#2f7a50");
            case Title.Princess() -> Color.web("#b04f78");
            case Title.Untitled() -> Color.web("#5b6472");
        };
        pen.setFill(new RadialGradient(0, 0, 0.5, 0.4, 0.75, true, CycleMethod.NO_CYCLE,
                new Stop(0, colour.brighter()), new Stop(1, colour.darker().darker())));
        pen.fillRect(0, 0, WIDTH, HEIGHT);
    }

    /** A head and shoulders, with long hair behind the head or short hair on top. */
    private void person(final GraphicsContext pen, final boolean longHair) {
        pen.setFill(Color.web("#3b2a1e"));
        if (longHair) {
            pen.fillOval(MIDDLE - 50, HEAD_Y - 52, 100, 128);
        }
        pen.setFill(Color.web("#1d2028"));
        pen.fillRoundRect(MIDDLE - 74, HEAD_Y + 54, 148, 140, 110, 110);
        pen.setFill(Color.web("#e7d8bd"));
        pen.fillRect(MIDDLE - 13, HEAD_Y + 30, 26, 30);
        pen.fillOval(MIDDLE - 36, HEAD_Y - 44, 72, 88);
        if (!longHair) {
            pen.setFill(Color.web("#3b2a1e"));
            pen.fillArc(MIDDLE - 37, HEAD_Y - 46, 74, 50, 0, 180, ArcType.CHORD);
        }
    }

    /** A crown: five points over a band, jewels on the points. */
    private void crown(final GraphicsContext pen) {
        final double base = HEAD_Y - 34;
        pen.setFill(gold());
        pen.fillPolygon(new double[] {MIDDLE - 42, MIDDLE - 42, MIDDLE - 21, MIDDLE, MIDDLE + 21, MIDDLE + 42,
                MIDDLE + 42}, new double[] {base + 12, base - 30, base - 12, base - 38, base - 12, base - 30,
                base + 12}, 7);
        pen.setFill(Color.web("#c0283c"));
        pen.fillOval(MIDDLE - 47, base - 35, 10, 10);
        pen.fillOval(MIDDLE - 5, base - 43, 10, 10);
        pen.fillOval(MIDDLE + 37, base - 35, 10, 10);
        pen.setFill(Color.web("#2f7de1"));
        pen.fillOval(MIDDLE - 6, base - 2, 12, 9);
    }

    /** A coronet: a low band with a row of pearls along its top. */
    private void coronet(final GraphicsContext pen) {
        final double base = HEAD_Y - 36;
        pen.setFill(gold());
        pen.fillRect(MIDDLE - 34, base, 68, 11);
        for (int i = -2; i <= 2; i++) {
            pen.fillOval(MIDDLE + i * 15 - 5, base - 9, 10, 10);
        }
    }

    /** A tiara: a slender arc rising to a point in the middle. */
    private void tiara(final GraphicsContext pen) {
        final double base = HEAD_Y - 32;
        pen.setFill(Color.web("#e9eef6"));
        pen.fillPolygon(new double[] {MIDDLE - 32, MIDDLE - 16, MIDDLE, MIDDLE + 16, MIDDLE + 32, MIDDLE + 26,
                MIDDLE - 26}, new double[] {base, base - 14, base - 26, base - 14, base, base + 5, base + 5}, 7);
        pen.setFill(Color.web("#7fc4ff"));
        pen.fillOval(MIDDLE - 5, base - 20, 10, 10);
    }

    /** The gold of crowns, coronets and captions. */
    private Color gold() {
        return Color.web("#d9b048");
    }

    /** The title, written under the person. */
    private void caption(final GraphicsContext pen, final Title title) {
        final String word = switch (title) {
            case Title.King() -> "KING";
            case Title.Queen() -> "QUEEN";
            case Title.Prince() -> "PRINCE";
            case Title.Princess() -> "PRINCESS";
            case Title.Untitled() -> "";
        };
        pen.setFill(gold());
        pen.setFont(Font.font("Serif", FontWeight.BOLD, 22));
        pen.setTextAlign(TextAlignment.CENTER);
        pen.setTextBaseline(VPos.CENTER);
        pen.fillText(word, MIDDLE, HEIGHT - 22);
    }
}
