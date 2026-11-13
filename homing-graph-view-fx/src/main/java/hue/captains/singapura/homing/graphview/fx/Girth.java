package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Scalar;

/**
 * How thick an edge is, from -1, the thinnest, through 0, the default, to 1, the thickest. The scale is exponential:
 * each step of girth makes an edge the same times thicker, three times from 0 to 1, so equal steps look equal. What a
 * girth measures is the view's, the same in every graph.
 *
 * @param value from -1 to 1, both included
 */
public record Girth(double value) implements Scalar {

    /** The radius of the middle girth, 0. */
    private static final double MIDDLE = 1.8;
    /** How many times thicker the thickest is than the middle, and the middle than the thinnest. */
    private static final double SPREAD = 3;

    /** Refuses a girth outside -1 to 1. */
    public Girth {
        if (!(value >= -1 && value <= 1)) {
            throw new IllegalArgumentException("A girth goes from -1 to 1, not " + value);
        }
    }

    /** The radius it draws an edge at, in scene units: 0.6 at -1, 1.8 at 0, 5.4 at 1. */
    double radius() {
        return MIDDLE * Math.pow(SPREAD, value);
    }
}
