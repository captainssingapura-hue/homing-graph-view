package hue.captains.singapura.homing.graphview.bench;

import hue.captains.singapura.homing.graphview.layout.Arrangement;
import hue.captains.singapura.homing.graphview.layout.Block;
import hue.captains.singapura.homing.graphview.layout.Ladder;
import hue.captains.singapura.homing.graphview.model.Vertices;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import java.util.List;
import java.util.stream.Collectors;

/** What the bench says: the layers to pick from, and of the layer shown, its block and the ladder to it. */
record Readout() implements StatelessFunctionalObject {

    /**
     * How a layer is named in the picker.
     *
     * @param depth its depth
     * @param layer its vertices
     */
    String name(final int depth, final Vertices layer) {
        return "depth " + depth + "  ·  " + plural(layer.size(), "vertex", "vertices");
    }

    /**
     * The readout for a layer laid out.
     *
     * @param layers      the graph's layers, the base's first
     * @param depth       the layer shown
     * @param arrangement its arrangement
     * @param ladder      the ladder its block was taken from
     */
    String of(final List<Vertices> layers, final int depth, final Arrangement arrangement, final Ladder ladder) {
        final int count = arrangement.size();
        final Block block = arrangement.block();
        final Block step = ladder.holding(count);
        final String steps = ladder.sizes(count).stream().map(this::size).collect(Collectors.joining(" → "));
        return "depth " + depth + " of " + layers.size() + " layers, " + plural(count, "vertex", "vertices")
                + "   ·   block " + size(block) + (block.equals(step) ? ", a size on the ladder"
                : ", the ladder's " + size(step) + " cut to the rows filled") + "   ·   ladder " + steps;
    }

    private String size(final Block block) {
        return block.columns() + "×" + block.rows();
    }

    private String plural(final int count, final String one, final String many) {
        return count + " " + (count == 1 ? one : many);
    }
}
