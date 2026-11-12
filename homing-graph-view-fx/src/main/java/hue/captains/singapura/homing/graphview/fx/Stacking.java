package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.layout.Ladder;
import hue.captains.singapura.homing.graphview.layout.Placed;
import hue.captains.singapura.homing.graphview.layout.Placement;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.FunctionalObject;
import javafx.geometry.Point3D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Sets a placement's layers, each flat in its block, in the 3D space, and stacks them in order, each on a platform. A
 * slot is a figure's room and a gap round it: across, and up when standing or back when lying. Standing, a layer's
 * figures hang on a board behind them; lying, they sit on a floor, its rows far enough apart for each to show over the
 * one in front. Every layer is centred on the stack's axis, a straight line through the middle of every platform: back
 * through the stack when standing, up it when lying, so the stack looks even from the side as from the front.
 *
 * <p>A block is shaped like a 16:9 screen, upright when standing and laid flat when lying. Each layer keeps clear of
 * the next as the starting camera sees them, looking straight in at its pitch: a standing layer stands far enough
 * behind the one in front to show over it, and a lying layer lies high enough above the one below for the whole floor
 * below to show under it; how far depends on both, as each reaches half its size either side of the axis.
 *
 * @param stance how the blocks are set
 * @param order  which end of the stack level 0 goes at
 * @param pitch  the starting camera's pitch, in degrees, negative looking down
 * @param room   the room every figure fits in
 */
record Stacking(Stance stance, Order order, double pitch, Room room) implements FunctionalObject {

    /** The gap round a figure in its slot: across, up when standing, and back when lying. */
    static final double ACROSS = 40;
    static final double UP = 34;
    static final double BACK = 116;
    /** A platform's thickness, and how far it reaches beyond the figures on it. */
    static final double THICKNESS = 12;
    static final double MARGIN = 24;
    /** The least distance from one standing layer to the next, and from one lying layer up to the next. */
    static final double LEAST_GAP = 300;
    static final double LEAST_RISE = 180;
    private static final double SCREEN = 16.0 / 9;
    private static final double GROWTH = 1.5;
    private static final double STANDING_YAW = -28;
    /** Room beyond what looking straight in needs, for the camera's perspective: far things look flatter. */
    private static final double CLEARANCE = 1.35;

    /** A slot's width. */
    double column() {
        return room.width() + ACROSS;
    }

    /** A slot's height when standing. */
    double row() {
        return room.height() + UP;
    }

    /** A slot's depth when lying: enough for each row to show over the one in front, from the starting pitch. */
    double rowDeep() {
        return Math.max(room.depth() + BACK, room.height() / slope() + room.depth());
    }

    /** The ladder a layer's block is taken from: a screen's shape in slots of the stance's size. */
    Ladder ladder() {
        return new Ladder(column() / (stance instanceof Stance.Standing ? row() : rowDeep()), SCREEN, GROWTH);
    }

    /** The starting camera's turn: standing, a little from the side; lying, straight at the front of the stack. */
    double yaw() {
        return stance instanceof Stance.Standing ? STANDING_YAW : 0;
    }

    /** How far below a figure's centre the stage reaches: half a figure, a margin and a platform. */
    double below() {
        return room.height() / 2 + MARGIN + THICKNESS;
    }

    /**
     * Where each figure is centred, in the placement's order.
     *
     * @param placement the layers laid out with this stacking's ladder
     */
    Map<Vertex, Point3D> of(final Placement placement) {
        final double[] along = along(placement);
        final Map<Vertex, Point3D> centres = new LinkedHashMap<>();
        for (final Placed placed : placement.items()) {
            final double x = placed.lane() * column();
            final double fromMiddle = placed.row() - (Math.max(1, placement.rowsOf(placed.depth())) - 1) / 2.0;
            centres.put(placed.vertex(), stance instanceof Stance.Standing
                    ? new Point3D(x, -fromMiddle * row(), along[placed.depth()])
                    : new Point3D(x, -along[placed.depth()], fromMiddle * rowDeep()));
        }
        return centres;
    }

    /**
     * Each layer's platform, level 0's first: as wide as its figures reach, with a margin all round, centred on the
     * stack's axis.
     *
     * @param placement the layers laid out with this stacking's ladder
     */
    List<Deck> decks(final Placement placement) {
        final double[] along = along(placement);
        final List<Deck> decks = new ArrayList<>();
        for (int depth = 0; depth <= placement.maxDepth(); depth++) {
            final int layer = depth;
            final List<Double> lanes = placement.items().stream().filter(placed -> placed.depth() == layer)
                    .map(Placed::lane).toList();
            final double low = lanes.stream().mapToDouble(Double::doubleValue).min().orElse(0);
            final double high = lanes.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            final double x = (low + high) / 2 * column();
            final double width = (high - low) * column() + room.width() + 2 * MARGIN;
            final int rows = Math.max(1, placement.rowsOf(depth));
            decks.add(stance instanceof Stance.Standing
                    ? new Deck(depth, new Point3D(x, 0, along[depth] + room.depth() / 2 + THICKNESS / 2),
                            new Point3D(width, tall(rows), THICKNESS))
                    : new Deck(depth, new Point3D(x, -along[depth] + room.height() / 2 + THICKNESS / 2, 0),
                            new Point3D(width, THICKNESS, deep(rows))));
        }
        return decks;
    }

    /** How far along the stack each layer sits: standing, back from the front; lying, up from the bottom. */
    private double[] along(final Placement placement) {
        final List<Integer> sequence = sequence(placement.maxDepth());
        final double[] along = new double[placement.maxDepth() + 1];
        for (int i = 1; i < sequence.size(); i++) {
            final int before = sequence.get(i - 1);
            final int next = sequence.get(i);
            along[next] = along[before]
                    + step(Math.max(1, placement.rowsOf(before)), Math.max(1, placement.rowsOf(next)));
        }
        return along;
    }

    /** The layers by depth from the start of the stack: from the front when standing, from the bottom when lying. */
    private List<Integer> sequence(final int deepest) {
        final List<Integer> baseFirst = IntStream.rangeClosed(0, deepest).boxed().toList();
        return stance instanceof Stance.Lying == order instanceof Order.BaseFar ? baseFirst : baseFirst.reversed();
    }

    /**
     * How far a layer of these rows sits from the one before it, of those, for the whole of the one before to show past
     * it: each reaches half its platform either side of the axis.
     */
    private double step(final int before, final int after) {
        return stance instanceof Stance.Standing
                ? Math.max(LEAST_GAP, CLEARANCE * ((tall(before) + tall(after)) / 2 / slope() + room.depth()
                        + THICKNESS))
                : Math.max(LEAST_RISE,
                        CLEARANCE * (room.height() + THICKNESS + slope() * (deep(before) + deep(after)) / 2));
    }

    /** The tangent of the starting camera's pitch: how far it sees down for each unit it sees ahead. */
    private double slope() {
        return Math.tan(Math.toRadians(-pitch));
    }

    /** How tall a standing platform of this many rows is. */
    private double tall(final int rows) {
        return (rows - 1) * row() + room.height() + 2 * MARGIN;
    }

    /** How deep a lying platform of this many rows is. */
    private double deep(final int rows) {
        return (rows - 1) * rowDeep() + room.depth() + 2 * MARGIN;
    }
}
