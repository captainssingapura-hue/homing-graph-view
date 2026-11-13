package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Scalar;
import javafx.scene.paint.Color;

/**
 * An opaque colour, red, green and blue from 0 to 255. Nothing is see-through: a scene in depth draws see-through
 * parts out of order.
 *
 * @param red   from 0 to 255
 * @param green from 0 to 255
 * @param blue  from 0 to 255
 */
public record Colour(int red, int green, int blue) implements Scalar {

    private static final int MOST = 255;
    private static final int BYTE = 8;

    /** Refuses a channel outside 0 to 255. */
    public Colour {
        if (red < 0 || red > MOST || green < 0 || green > MOST || blue < 0 || blue > MOST) {
            throw new IllegalArgumentException("A colour's red, green and blue go from 0 to 255, not " + red + ", "
                    + green + " and " + blue);
        }
    }

    /**
     * A colour written as a web colour is, in hex: {@code new Colour(0xd4af37)} for #d4af37.
     *
     * @param rgb red, green and blue, a byte each, from 0x000000 to 0xffffff
     */
    public Colour(final int rgb) {
        this(rgb >> (2 * BYTE), (rgb >> BYTE) & MOST, rgb & MOST);
    }

    /** The colour, as JavaFX paints it. */
    Color color() {
        return Color.rgb(red, green, blue);
    }
}
