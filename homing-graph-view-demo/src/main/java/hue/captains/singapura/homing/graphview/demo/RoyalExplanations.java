package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.viewer.Explanation;
import hue.captains.singapura.homing.graphview.viewer.Explanations;

/**
 * What the viewer says of a member of the royal family: who they are, from their line in {@code royal-family.txt},
 * then their parents, whom they married and their children, read from the graph.
 */
record RoyalExplanations() implements Explanations {

    @Override
    public Explanation of(final Vertex vertex, final Graph graph) {
        return new Explanation(vertex.id(), new Biographies().of(vertex.id()) + "\n\n" + new Kin().of(graph, vertex));
    }
}
