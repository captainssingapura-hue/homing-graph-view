package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.FunctionalObject;

/**
 * What a selection means: asked of a focus, not wired into the renderer. The library's default is Reachability; an
 * adapter supplies its own, holding whatever else it needs to know.
 */
public interface Focus extends FunctionalObject {

    /**
     * What selecting a vertex shows.
     *
     * @param graph the graph shown
     * @param root  the vertex selected, one of the graph's
     */
    Highlight of(Graph graph, Vertex root);
}
