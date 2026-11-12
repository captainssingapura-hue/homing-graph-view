package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.layout.Highlight;
import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import java.util.Optional;

/** The state a selection puts each vertex and edge in. The renderer knows only these; the focus decides them. */
public record States() implements StatelessFunctionalObject {

    /** A vertex's state under the selection, idle when there is none. */
    public NodeState of(final Optional<Highlight> selection, final Vertex vertex) {
        return selection.map(highlight -> {
            if (highlight.root().equals(vertex)) {
                return new NodeState.Selected();
            }
            if (highlight.members().contains(vertex)) {
                return new NodeState.Member();
            }
            return highlight.marked().contains(vertex) ? new NodeState.Marked() : new NodeState.Dim();
        }).orElse(new NodeState.Idle());
    }

    /** An edge's state under the selection, idle when there is none. */
    public EdgeState of(final Optional<Highlight> selection, final Edge edge) {
        return selection.map(highlight -> {
            if (highlight.lit().contains(edge)) {
                return new EdgeState.Lit();
            }
            return highlight.markedEdges().contains(edge) ? new EdgeState.Marked() : new EdgeState.Dim();
        }).orElse(new EdgeState.Idle());
    }
}
