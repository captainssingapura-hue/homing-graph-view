package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.layout.Ladder;
import hue.captains.singapura.homing.graphview.layout.Placed;
import hue.captains.singapura.homing.graphview.layout.Placement;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.FunctionalObject;
import javafx.geometry.Point3D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sets a placement's layers, each flat in its block, in the 3D space in a stance, and stacks them. A slot is a slate
 * and the room around it: {@value #COLUMN} across, and {@value #ROW} up when standing or {@value #ROW_DEEP} back when
 * lying.
 *
 * <p>The ladder, for the layout to lay the layers out with, shapes a block to look 16:9 from the starting camera: a
 * standing block is 16:9; a lying one is deeper, as the camera sees its depth foreshortened by the sine of its pitch.
 * Each layer keeps clear of the next as the starting camera sees them, looking straight in at its pitch: a standing
 * layer stands far enough behind the one in front to show over it, and a lying layer lies high enough above the one
 * below to show beyond it.
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
     * Where each slate is centred, in the placement's order.
     *
     * @param placement the layers laid out with this stacking's ladder
     */
    Map<Vertex, Point3D> of(final Placement placement) {
        final double[] along = along(placement);
        final Map<Vertex, Point3D> centres = new LinkedHashMap<>();
        for (final Placed placed : placement.items()) {
            final double x = placed.lane() * COLUMN;
            centres.put(placed.vertex(), stance instanceof Stance.Standing
                    ? new Point3D(x, -placed.row() * ROW, along[placed.depth()])
                    : new Point3D(x, -along[placed.depth()], placed.row() * ROW_DEEP));
        }
        return centres;
    }

    /**
     * How far along the stack each layer sits: standing, back from the root's layer at 0; lying, up from the base's.
     */
    private double[] along(final Placement placement) {
        final double slope = Math.tan(Math.toRadians(-pitch));
        final int deepest = placement.maxDepth();
        final double[] along = new double[deepest + 1];
        if (stance instanceof Stance.Standing) {
            for (int depth = deepest - 1; depth >= 0; depth--) {
                along[depth] = along[depth + 1] + Math.max(LEAST_GAP, placement.rowsOf(depth + 1) * ROW / slope);
            }
        } else {
            for (int depth = 1; depth <= deepest; depth++) {
                along[depth] = along[depth - 1]
                        + Math.max(LEAST_GAP, placement.rowsOf(depth - 1) * ROW_DEEP * slope + Slate.HEIGHT);
            }
        }
        return along;
    }
}
