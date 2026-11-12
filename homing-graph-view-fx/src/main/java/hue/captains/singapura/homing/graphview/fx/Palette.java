package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import javafx.scene.paint.Color;

/**
 * The default look: a neutral palette on a dark background, with no kinds. The marked colour is amber, for "reached but
 * passed over". Themes replace it later.
 */
public record Palette() implements StatelessFunctionalObject {

    /** Behind the graph. */
    public Color background() {
        return Color.web("#14171c");
    }

    /** A slate's colour in each state. */
    public Color slate(final NodeState state) {
        return switch (state) {
            case NodeState.Idle() -> Color.web("#3d4b5f");
            case NodeState.Selected() -> Color.web("#2f7de1");
            case NodeState.Member() -> Color.web("#2f7a5c");
            case NodeState.Marked() -> Color.web("#c48a22");
            case NodeState.Dim() -> Color.web("#23272e");
        };
    }

    /** The text on a container's faces in each state. */
    public Color text(final NodeState state) {
        return state instanceof NodeState.Dim ? Color.web("#6b7280") : Color.web("#eef2f7");
    }

    /**
     * A layer's platform, by its depth: the soft colours a political map gives its states, taken in turn, so no two
     * layers next to each other share one.
     */
    public Color platform(final int depth) {
        final String[] map = {"#e3c9a8", "#b5cfa4", "#ecdc9c", "#a8c4de", "#d4b6d4", "#eeb7a3"};
        return Color.web(map[Math.floorMod(depth, map.length)]);
    }

    /** A rod's colour in each state. */
    public Color edge(final EdgeState state) {
        return switch (state) {
            case EdgeState.Idle() -> Color.web("#7b8698");
            case EdgeState.Lit() -> Color.web("#62b6ff");
            case EdgeState.Marked() -> Color.web("#e0a030");
            case EdgeState.Dim() -> Color.web("#2c3139");
        };
    }
}
