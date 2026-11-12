package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.FunctionalObject;

/** Which container each vertex comes in, told by whoever knows how big each vertex is. */
public interface Containers extends FunctionalObject {

    /**
     * The container a vertex comes in.
     *
     * @param vertex a vertex of the graph drawn
     */
    Container of(Vertex vertex);
}
