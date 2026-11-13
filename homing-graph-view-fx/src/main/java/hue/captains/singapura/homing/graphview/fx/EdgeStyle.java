package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Scalar;
import java.util.Objects;

/**
 * How an edge is drawn: as thick as its girth, in its own colour while nothing is selected, with its texture along it
 * and its head, if any, at the vertex depended on. Only these: whatever an edge is, the view draws it.
 *
 * @param girth   how thick, from the thinnest to the thickest
 * @param colour  its colour while nothing is selected; a selection draws it in the colour of its state
 * @param texture what it is along its length
 * @param head    what is at the end of the vertex depended on
 */
public record EdgeStyle(Girth girth, Colour colour, Texture texture, Head head) implements Scalar {

    /** Refuses a missing part. */
    public EdgeStyle {
        Objects.requireNonNull(girth, "An edge's style needs its girth");
        Objects.requireNonNull(colour, "An edge's style needs its colour");
        Objects.requireNonNull(texture, "An edge's style needs its texture");
        Objects.requireNonNull(head, "An edge's style needs its head, even if none");
    }

    /** The default: a plain grey rod of the middle girth, with no head. */
    public EdgeStyle() {
        this(new Girth(0), new Colour(0x7b8698), new Texture.Solid(), new Head.None());
    }

    /** This style with another girth. */
    public EdgeStyle with(final Girth other) {
        return new EdgeStyle(other, colour, texture, head);
    }

    /** This style with another colour. */
    public EdgeStyle with(final Colour other) {
        return new EdgeStyle(girth, other, texture, head);
    }

    /** This style with another texture. */
    public EdgeStyle with(final Texture other) {
        return new EdgeStyle(girth, colour, other, head);
    }

    /** This style with another head. */
    public EdgeStyle with(final Head other) {
        return new EdgeStyle(girth, colour, texture, other);
    }
}
