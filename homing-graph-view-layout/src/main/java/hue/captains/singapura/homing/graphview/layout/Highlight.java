package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.homing.graphview.model.Edges;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.model.Vertices;
import hue.captains.singapura.tao.ontology.Scalar;
import java.util.Objects;

/**
 * What a selection means, as a Focus says it: the vertex selected, the vertices it uses, those it reaches but does not
 * use, and the edges between them. A renderer draws the root as selected, members as lit, marked vertices in the marked
 * colour and the rest dimmed; why a vertex is in one state or another is the focus's business.
 *
 * @param root        the vertex selected
 * @param members     the root and the vertices it uses: lit
 * @param marked      vertices it reaches but does not use, none of them members
 * @param lit         edges between members
 * @param markedEdges edges from a member to a marked vertex
 */
public record Highlight(Vertex root, Vertices members, Vertices marked, Edges lit, Edges markedEdges)
        implements Scalar {

    /** Refuses a root that is not a member, and a vertex that is both a member and marked. */
    public Highlight {
        Objects.requireNonNull(root, "A highlight needs its root");
        Objects.requireNonNull(members, "A highlight needs its members");
        Objects.requireNonNull(marked, "A highlight needs its marked vertices, even if there are none");
        Objects.requireNonNull(lit, "A highlight needs its lit edges, even if there are none");
        Objects.requireNonNull(markedEdges, "A highlight needs its marked edges, even if there are none");
        if (!members.contains(root)) {
            throw new IllegalArgumentException("The root, " + root.id() + ", is not one of the members");
        }
        for (final Vertex vertex : marked.items()) {
            if (members.contains(vertex)) {
                throw new IllegalArgumentException("Vertex " + vertex.id() + " is both a member and marked");
            }
        }
    }
}
