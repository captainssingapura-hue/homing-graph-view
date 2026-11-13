package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.model.Edge;
import java.util.Objects;

/**
 * Every edge drawn alike, whatever the graph: the styler for a graph whose edges are all of a kind, and the view's
 * default, a plain grey rod.
 *
 * @param style the style of every edge
 */
public record SameStyle(EdgeStyle style) implements EdgeStyler {

    /** Refuses a missing style. */
    public SameStyle {
        Objects.requireNonNull(style, "Every edge alike needs the style they share");
    }

    /** Every edge a plain grey rod, as {@link EdgeStyle#EdgeStyle()} says. */
    public SameStyle() {
        this(new EdgeStyle());
    }

    @Override
    public EdgeStyle style(final Edge edge) {
        return style;
    }
}
