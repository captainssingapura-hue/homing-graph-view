package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Mutable;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import java.util.List;

/** The layers' platforms drawn: a slab for each, coloured by its layer as a map colours its states. */
final class Platforms implements Mutable {

    private final Group node = new Group();

    /**
     * The platforms, out of the clicks' way.
     *
     * @param decks   where each layer's platform is
     * @param palette the colours
     */
    Platforms(final List<Deck> decks, final Palette palette) {
        for (final Deck deck : decks) {
            final Box slab = new Box(deck.size().getX(), deck.size().getY(), deck.size().getZ());
            final PhongMaterial material = new PhongMaterial(palette.platform(deck.depth()));
            material.setSpecularColor(palette.platform(deck.depth()).darker());
            slab.setMaterial(material);
            slab.setTranslateX(deck.centre().getX());
            slab.setTranslateY(deck.centre().getY());
            slab.setTranslateZ(deck.centre().getZ());
            node.getChildren().add(slab);
        }
        node.setMouseTransparent(true);
    }

    /** The platforms' node in the scene. */
    Group node() {
        return node;
    }
}
