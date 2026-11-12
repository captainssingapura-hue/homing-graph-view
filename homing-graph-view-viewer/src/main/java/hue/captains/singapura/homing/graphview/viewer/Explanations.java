package hue.captains.singapura.homing.graphview.viewer;

import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.FunctionalObject;

/** What to say of each vertex of a graph when it is selected, told by whoever knows what the vertices are. */
public interface Explanations extends FunctionalObject {

    /**
     * The explanation of a vertex.
     *
     * @param vertex the vertex selected
     * @param graph  the graph it is in, for anything to say of its edges
     */
    Explanation of(Vertex vertex, Graph graph);
}
