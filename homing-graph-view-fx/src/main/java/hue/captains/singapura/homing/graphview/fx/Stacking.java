package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.layout.Arrangement;
import hue.captains.singapura.homing.graphview.layout.Ladder;
import hue.captains.singapura.homing.graphview.layout.LayerInBlock;
import hue.captains.singapura.homing.graphview.layout.Seat;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.model.Vertices;
import hue.captains.singapura.tao.ontology.FunctionalObject;
import javafx.geometry.Point3D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sets each layer, laid out flat in its block, in the 3D space in a stance, and stacks the layers. A slot is a slate and
 * the room around it: {@value #COLUMN} across, and {@value #ROW} up when standing or {@value #ROW_DEEP} back when lying.
 *
 * <p>The ladder shapes a block to look 16:9 from the starting camera: a standing block is 16:9; a lying one is deeper,
 * as the camera sees its depth foreshortened by the sine of its pitch. Each layer then keeps clear of the next as the
 * starting camera sees them, looking straight in at its pitch: a standing layer stands far enough behind the one in
 * front to show over it, and a lying layer lies high enough above the one below to show beyond it.
 *
 * @param stance how the blocks are set
 * @param pitch  the starting camera's pitch, in degrees, negative looking down
 */
record Stacking(Stance stance, double pitch) implements FunctionalObject {

    /** A slot's width, its height standing and its depth lying. */
    static final double COLUMN = 230;
    static final double ROW = 80;
    static final double ROW_DEEP = 160;
    /** The least distance from one layer to the next. */
    static final double LEAST_GAP = 300;
    private static final double SCREEN = 16.0 / 9;
    private static final double GROWTH = 1.5;

    /** The ladder a layer's block is taken from, by the stance's slot and the shape the camera should see. */
    Ladder ladder() {
        return switch (stance) {
            case Stance.Standing() -> new Ladder(COLUMN / ROW, SCREEN, GROWTH);
            case Stance.Lying() -> new Ladder(COLUMN / ROW_DEEP, SCREEN * Math.sin(Math.toRadians(-pitch)), GROWTH);
        };
    }

    /**
     * Where each slate is centred.
     *
     * @param layers the graph's layers, the base's first
     */
    Map<Vertex, Point3D> of(final List<Vertices> layers) {
        final LayerInBlock inBlock = new LayerInBlock(ladder());
        final List<Arrangement> blocks = layers.stream().map(inBlock::of).toList();
        final Map<Vertex, Point3D> centres = new LinkedHashMap<>();
        final double slope = Math.tan(Math.toRadians(-pitch));
        double along = 0;
        if (stance instanceof Stance.Standing) {
            for (int depth = blocks.size() - 1; depth >= 0; depth--) {
                for (final Seat seat : blocks.get(depth).items()) {
                    centres.put(seat.vertex(), new Point3D(seat.slot().lane() * COLUMN, -seat.slot().row() * ROW,
                            along));
                }
                along += Math.max(LEAST_GAP, blocks.get(depth).block().rows() * ROW / slope);
            }
        } else {
            for (final Arrangement block : blocks) {
                for (final Seat seat : block.items()) {
                    centres.put(seat.vertex(), new Point3D(seat.slot().lane() * COLUMN, -along,
                            seat.slot().row() * ROW_DEEP));
                }
                along += Math.max(LEAST_GAP, block.block().rows() * ROW_DEEP * slope + Slate.HEIGHT);
            }
        }
        return centres;
    }
}
