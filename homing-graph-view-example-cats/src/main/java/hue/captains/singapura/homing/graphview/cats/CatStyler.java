package hue.captains.singapura.homing.graphview.cats;

import hue.captains.singapura.homing.graphview.fx.Colour;
import hue.captains.singapura.homing.graphview.fx.EdgeStyle;
import hue.captains.singapura.homing.graphview.fx.EdgeStyler;
import hue.captains.singapura.homing.graphview.fx.Texture;
import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertices;
import java.util.Objects;

/**
 * The breeds' edges: a cross with a wild cat dashed, in a wild cat's amber, and any other breeding a plain grey rod.
 * Made for the studbook's graph, it finds which of its breeds are wild once.
 *
 * @param wild the wild cats among the graph's breeds
 */
record CatStyler(Vertices wild) implements EdgeStyler {

    private static final int AMBER = 0xb07a34;

    /** Refuses missing wild cats. */
    CatStyler {
        Objects.requireNonNull(wild, "The cat styler needs the wild cats, even if none");
    }

    /**
     * The styler for the breeds: the wild cats are those the studbook gives a wild coat.
     *
     * @param breeds the breeds, as the studbook gives them
     */
    CatStyler(final Graph breeds) {
        this(new Vertices(breeds.vertices().items().stream()
                .filter(breed -> new Studbook().breed(breed.id()).map(Breed::coat)
                        .filter(Coat.Wild.class::isInstance).isPresent())
                .toList()));
    }

    @Override
    public EdgeStyle style(final Edge edge) {
        if (wild.contains(edge.to())) {
            return new EdgeStyle().with(new Texture.Dashed()).with(new Colour(AMBER));
        }
        return new EdgeStyle();
    }
}
