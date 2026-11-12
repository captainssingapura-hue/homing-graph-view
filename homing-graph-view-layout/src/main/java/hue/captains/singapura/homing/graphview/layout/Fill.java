package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * How a list fills a block, as it fills an array: in its order, a row at a time from the top left, the rows it fills
 * standing on the bottom of the block. A last row that is not full is centred.
 */
record Fill() implements StatelessFunctionalObject {

    /**
     * Where the item at this index goes.
     *
     * @param index its index in the list
     * @param count how many the list holds, no more than the block does
     * @param block the block
     */
    Slot of(final int index, final int count, final Block block) {
        if (index < 0 || index >= count || count > block.capacity()) {
            throw new IllegalArgumentException("Item " + index + " of " + count + " has no slot in a block of "
                    + block.columns() + " by " + block.rows());
        }
        final int columns = block.columns();
        final int rows = block.rowsFor(count);
        final int down = index / columns;
        final int inRow = down < rows - 1 ? columns : count - columns * (rows - 1);
        return new Slot(index % columns - (inRow - 1) / 2.0, rows - 1 - down);
    }
}
