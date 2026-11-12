package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.tao.ontology.Scalar;

/**
 * A block of slots, columns across and rows up: the rectangle a layer's vertices are arranged in. Like an ArrayList's
 * array, it may hold more than it is given.
 *
 * @param columns its slots across, at least 1
 * @param rows    its slots up, at least 1
 */
public record Block(int columns, int rows) implements Scalar {

    /** Refuses a block without a slot each way. */
    public Block {
        if (columns < 1 || rows < 1) {
            throw new IllegalArgumentException("A block of " + columns + " by " + rows
                    + " slots holds nothing; it needs at least one slot each way");
        }
    }

    /** How many slots it has. */
    public int capacity() {
        return columns * rows;
    }

    /** How many of its rows this many vertices fill, a row at a time: none for none. */
    public int rowsFor(final int count) {
        return (count + columns - 1) / columns;
    }
}
