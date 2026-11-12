package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.tao.ontology.FunctionalObject;
import java.util.ArrayList;
import java.util.List;

/**
 * The sizes a block comes in, as an ArrayList's capacities do: the first is a single slot, and each one after holds at
 * least {@code growth} times as many as the one before, shaped as nearly like the screen as whole slots allow. The size
 * a graph needs is known before anything is placed, from its largest layer.
 *
 * @param slot   a slot's width over its height
 * @param screen the screen's width over its height: 16/9 for a monitor
 * @param growth how many times as many each size holds as the one before, more than 1: an ArrayList's is 1.5
 */
public record Ladder(double slot, double screen, double growth) implements FunctionalObject {

    /** Refuses a shape that is not a positive number, and growth that does not grow. */
    public Ladder {
        if (!(slot > 0 && Double.isFinite(slot) && screen > 0 && Double.isFinite(screen))) {
            throw new IllegalArgumentException("A ladder needs a slot and a screen of positive width over height, not "
                    + slot + " and " + screen);
        }
        if (!(growth > 1 && Double.isFinite(growth))) {
            throw new IllegalArgumentException("Each size must hold more than the one before; growth " + growth
                    + " does not grow");
        }
    }

    /**
     * The smallest size that holds this many vertices: a single slot for none or one.
     *
     * @param count how many, not negative
     */
    public Block holding(final int count) {
        return sizes(count).getLast();
    }

    /**
     * The sizes in order, from a single slot up to the first that holds this many vertices.
     *
     * @param count how many, not negative
     */
    public List<Block> sizes(final int count) {
        if (count < 0) {
            throw new IllegalArgumentException("No block holds " + count + " vertices");
        }
        final List<Block> sizes = new ArrayList<>(List.of(new Block(1, 1)));
        while (sizes.getLast().capacity() < count) {
            sizes.add(after(sizes.getLast()));
        }
        return List.copyOf(sizes);
    }

    /** The size after this one: the fewest columns whose block of the screen's shape holds growth times as many. */
    private Block after(final Block block) {
        final double wanted = block.capacity() * growth;
        int columns = block.columns();
        while (shaped(columns).capacity() < wanted) {
            columns++;
        }
        return shaped(columns);
    }

    /** The block this many columns wide that is most nearly the screen's shape. */
    private Block shaped(final int columns) {
        return new Block(columns, Math.max(1, (int) Math.round(columns * slot / screen)));
    }
}
