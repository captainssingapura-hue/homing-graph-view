package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.tao.ontology.FunctionalObject;

/**
 * How the edges of one graph are drawn: made downstream for that graph and handed to the view with it, which asks it of
 * each of the graph's edges and of nothing else. A styler can work out what it needs of its graph when it is made, its
 * marriages, say; it says only an edge's style, and the view draws it.
 */
public interface EdgeStyler extends FunctionalObject {

    /**
     * How an edge is drawn.
     *
     * @param edge an edge of the graph this styler was made for
     */
    EdgeStyle style(Edge edge);
}
