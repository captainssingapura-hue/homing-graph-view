package hue.captains.singapura.homing.graphview.viewer;

import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;

/** The plainest explanation: a vertex's name, its ID, and nothing more. */
public record Names() implements Explanations {

    @Override
    public Explanation of(final Vertex vertex, final Graph graph) {
        return new Explanation(vertex.id(), "");
    }
}
