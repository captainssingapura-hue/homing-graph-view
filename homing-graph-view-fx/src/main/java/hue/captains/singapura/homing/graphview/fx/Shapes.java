package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.FunctionalObject;

/**
 * What each vertex is drawn as: a figure for a vertex, and the room every figure fits in, which the layout sizes its
 * slots by. The default draws containers; a provider of its own can draw anything, a photo frame for a person, say.
 */
public interface Shapes extends FunctionalObject {

    /** The room every figure fits in. */
    Room room();

    /**
     * The figure for a vertex: a new node each time, as a node lives in one place in a scene.
     *
     * @param vertex a vertex of the graph drawn
     */
    Figure of(Vertex vertex);
}
