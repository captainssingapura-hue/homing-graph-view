package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.homing.graphview.fx.Colour;
import hue.captains.singapura.homing.graphview.fx.EdgeStyle;
import hue.captains.singapura.homing.graphview.fx.EdgeStyler;
import hue.captains.singapura.homing.graphview.fx.Girth;
import hue.captains.singapura.homing.graphview.fx.Texture;
import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Edges;
import hue.captains.singapura.homing.graphview.model.Graph;
import java.util.Objects;

/**
 * The royal family's edges: a marriage, a pair of edges one each way, as a gold double line, as a family tree draws
 * one, and a child's edge to each parent as a thin grey rod. Made for the family's graph, it finds its marriages once.
 *
 * @param marriages the edges of the graph whose reverse is in it too
 */
record RoyalStyler(Edges marriages) implements EdgeStyler {

    private static final int GOLD = 0xd4af37;
    private static final double MARRIAGE = 0.5;
    private static final double PARENTAGE = -0.5;

    /** Refuses missing marriages. */
    RoyalStyler {
        Objects.requireNonNull(marriages, "The royal styler needs the family's marriages, even if none");
    }

    /**
     * The styler for a family: its marriages are its pairs of edges each way.
     *
     * @param family the family, as {@code RoyalFamily} gives it
     */
    RoyalStyler(final Graph family) {
        this(new Edges(family.edges().items().stream()
                .filter(edge -> family.edges().contains(new Edge(edge.to(), edge.from())))
                .toList()));
    }

    @Override
    public EdgeStyle style(final Edge edge) {
        if (marriages.contains(edge)) {
            return new EdgeStyle().with(new Texture.Twin()).with(new Colour(GOLD)).with(new Girth(MARRIAGE));
        }
        return new EdgeStyle().with(new Girth(PARENTAGE));
    }
}
