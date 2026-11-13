package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.layout.Highlight;
import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Edges;
import hue.captains.singapura.tao.ontology.Mutable;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The edges drawn, each as its styler says, along its span: in its own colour while nothing is selected, and otherwise
 * in the colour of the state the selection puts it in. Edges take no clicks, so a click on one reaches what is behind
 * it.
 */
final class Rods implements Mutable {

    private final Group node = new Group();
    private final Map<Edge, PhongMaterial> materials = new LinkedHashMap<>();
    private final Map<Edge, Color> own = new LinkedHashMap<>();

    /**
     * The edges, drawn.
     *
     * @param edges  the edges
     * @param spans  where each is drawn
     * @param styler how each is drawn
     */
    Rods(final Edges edges, final Spans spans, final EdgeStyler styler) {
        final Strand strand = new Strand();
        for (final Edge edge : edges.items()) {
            final EdgeStyle style = styler.style(edge);
            final MeshView view = new MeshView(strand.of(style, spans.start(edge), spans.end(edge)).mesh());
            final PhongMaterial material = new PhongMaterial(style.colour().color());
            view.setMaterial(material);
            view.setUserData(edge);
            materials.put(edge, material);
            own.put(edge, style.colour().color());
            node.getChildren().add(view);
        }
        node.setMouseTransparent(true);
    }

    /** The edges' node in the scene. */
    Group node() {
        return node;
    }

    /** Draws every edge in the state the selection puts it in. */
    void paint(final Optional<Highlight> selection, final States states, final Palette palette) {
        materials.forEach((edge, material) -> {
            final EdgeState state = states.of(selection, edge);
            material.setDiffuseColor(state instanceof EdgeState.Idle ? own.get(edge) : palette.edge(state));
        });
    }
}
