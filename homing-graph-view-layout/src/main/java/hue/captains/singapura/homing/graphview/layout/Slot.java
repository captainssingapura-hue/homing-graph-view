package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.tao.ontology.Scalar;

/**
 * A place in a block: its lane across, centred on 0, lanes 1 apart, and its row up, from 0 at the bottom.
 *
 * @param lane its lane across the block
 * @param row  its row up the block
 */
public record Slot(double lane, int row) implements Scalar {

    /** Refuses a lane that is not a number and a row below the bottom. */
    public Slot {
        if (!Double.isFinite(lane) || row < 0) {
            throw new IllegalArgumentException("There is no slot in lane " + lane + ", row " + row);
        }
    }
}
