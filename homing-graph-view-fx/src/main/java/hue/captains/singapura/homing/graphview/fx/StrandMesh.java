package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Mutable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableFloatArray;
import javafx.collections.ObservableIntegerArray;
import javafx.geometry.Point3D;
import javafx.scene.shape.TriangleMesh;
import java.util.List;

/**
 * The pieces of one edge, tubes, cones and beads, gathered into a single mesh, so an edge in dashes is one node as a
 * solid one is. Each piece is closed and convex, and each of its triangles faces out of it; its round sides are drawn
 * smooth and its ends flat.
 */
final class StrandMesh implements Mutable {

    /** Sides around a tube or a cone, as many as the rods had. */
    private static final int SIDES = 8;
    /** Bands of a bead from pole to pole. */
    private static final int BANDS = 4;
    private static final int SMOOTH = 1;
    private static final int FLAT = 2;
    private static final int XYZ = 3;
    private static final double NEARLY_ALONG = 0.9;

    private final ObservableFloatArray points = FXCollections.observableFloatArray();
    private final ObservableIntegerArray faces = FXCollections.observableIntegerArray();
    private final ObservableIntegerArray smoothing = FXCollections.observableIntegerArray();
    private int pieces;

    /** A tube from one point to another: nothing, if they are the same point. */
    void tube(final Point3D from, final Point3D to, final double radius) {
        final Point3D along = to.subtract(from);
        if (!(along.magnitude() > 0)) {
            return;
        }
        final List<Point3D> across = across(along);
        final int start = ring(from, across, radius);
        final int end = ring(to, across, radius);
        final int startCentre = point(from);
        final int endCentre = point(to);
        final Point3D inside = from.midpoint(to);
        for (int side = 0; side < SIDES; side++) {
            final int next = (side + 1) % SIDES;
            face(start + side, end + next, end + side, SMOOTH, inside);
            face(start + side, start + next, end + next, SMOOTH, inside);
            face(startCentre, start + next, start + side, FLAT, inside);
            face(endCentre, end + side, end + next, FLAT, inside);
        }
        pieces++;
    }

    /** A cone standing on a round base, up to its tip: nothing, if they are the same point. */
    void cone(final Point3D base, final Point3D tip, final double radius) {
        final Point3D along = tip.subtract(base);
        if (!(along.magnitude() > 0)) {
            return;
        }
        final int ring = ring(base, across(along), radius);
        final int centre = point(base);
        final int top = point(tip);
        final Point3D inside = base.add(along.multiply(1.0 / XYZ));
        for (int side = 0; side < SIDES; side++) {
            final int next = (side + 1) % SIDES;
            face(ring + side, ring + next, top, SMOOTH, inside);
            face(centre, ring + next, ring + side, FLAT, inside);
        }
        pieces++;
    }

    /** A bead, a ball of a few bands, centred on a point. */
    void bead(final Point3D centre, final double radius) {
        final int north = point(centre.add(0, -radius, 0));
        final int bands = points.size() / XYZ;
        for (int band = 1; band < BANDS; band++) {
            final double down = Math.PI * band / BANDS;
            for (int side = 0; side < SIDES; side++) {
                final double around = 2 * Math.PI * side / SIDES;
                point(centre.add(radius * Math.sin(down) * Math.cos(around), -radius * Math.cos(down),
                        radius * Math.sin(down) * Math.sin(around)));
            }
        }
        final int south = point(centre.add(0, radius, 0));
        for (int side = 0; side < SIDES; side++) {
            final int next = (side + 1) % SIDES;
            face(north, bands + side, bands + next, SMOOTH, centre);
            for (int band = 0; band < BANDS - 2; band++) {
                final int upper = bands + band * SIDES;
                final int lower = upper + SIDES;
                face(upper + side, lower + side, lower + next, SMOOTH, centre);
                face(upper + side, lower + next, upper + next, SMOOTH, centre);
            }
            final int last = bands + (BANDS - 2) * SIDES;
            face(south, last + next, last + side, SMOOTH, centre);
        }
        pieces++;
    }

    /** How many pieces there are. */
    int pieces() {
        return pieces;
    }

    /** Every piece's corners, x, y and z in turn. */
    ObservableFloatArray points() {
        return points;
    }

    /** The pieces, as one mesh to draw. */
    TriangleMesh mesh() {
        final TriangleMesh mesh = new TriangleMesh();
        mesh.getTexCoords().addAll(0, 0);
        mesh.getPoints().addAll(points);
        mesh.getFaces().addAll(faces);
        mesh.getFaceSmoothingGroups().addAll(smoothing);
        return mesh;
    }

    /** Two directions square to the line and to each other, to go round it by. */
    private List<Point3D> across(final Point3D along) {
        final Point3D axis = along.normalize();
        final Point3D other = Math.abs(axis.getX()) < NEARLY_ALONG ? new Point3D(1, 0, 0) : new Point3D(0, 1, 0);
        final Point3D first = axis.crossProduct(other).normalize();
        return List.of(first, axis.crossProduct(first));
    }

    /** A ring of points round a centre; the first one's index. */
    private int ring(final Point3D centre, final List<Point3D> across, final double radius) {
        final int first = points.size() / XYZ;
        for (int side = 0; side < SIDES; side++) {
            final double around = 2 * Math.PI * side / SIDES;
            point(centre.add(across.get(0).multiply(radius * Math.cos(around)))
                    .add(across.get(1).multiply(radius * Math.sin(around))));
        }
        return first;
    }

    private int point(final Point3D point) {
        points.addAll((float) point.getX(), (float) point.getY(), (float) point.getZ());
        return points.size() / XYZ - 1;
    }

    /** A triangle, turned if need be to face away from a point inside its piece. */
    private void face(final int first, final int second, final int third, final int group, final Point3D inside) {
        final Point3D a = at(first);
        final Point3D facing = at(second).subtract(a).crossProduct(at(third).subtract(a));
        final boolean outward = facing.dotProduct(a.subtract(inside)) >= 0;
        faces.addAll(first, 0, outward ? second : third, 0, outward ? third : second, 0);
        smoothing.addAll(group);
    }

    private Point3D at(final int index) {
        return new Point3D(points.get(index * XYZ), points.get(index * XYZ + 1), points.get(index * XYZ + 2));
    }
}
