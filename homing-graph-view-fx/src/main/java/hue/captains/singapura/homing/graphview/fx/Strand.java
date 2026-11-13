package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import javafx.geometry.Point3D;

/**
 * An edge drawn in its style, from where it starts to the end at the vertex depended on: a solid tube, tubes in dashes,
 * beads, or two thinner tubes side by side, as thick as its girth, and an arrowhead at the end if it has one. Dashes,
 * beads and heads grow with the girth, though less than it does, so a thin edge's still show.
 */
record Strand() implements StatelessFunctionalObject {

    private static final double DASH = 16;
    private static final double DASH_PER_RADIUS = 6;
    private static final double GAP = 10;
    private static final double GAP_PER_RADIUS = 3;
    private static final double BEAD = 1;
    private static final double BEAD_PER_RADIUS = 1.6;
    private static final double BEADS_APART = 12;
    private static final double BEADS_APART_PER_RADIUS = 5;
    private static final double TWIN = 0.6;
    private static final double TWINS_APART = 1.2;
    private static final double TWINS_APART_PER_RADIUS = 0.8;
    private static final double HEAD = 8;
    private static final double HEAD_PER_RADIUS = 5;
    private static final double HEAD_WIDTH = 2.5;
    private static final double HEAD_WIDTH_PER_RADIUS = 1.6;
    private static final double HEAD_OVER_TWINS = 1.3;
    private static final double MOST_OF_IT_HEAD = 0.6;
    private static final double LEVEL = 0.2;

    /**
     * The edge's pieces, in one mesh.
     *
     * @param style how the edge is drawn
     * @param start where it starts
     * @param end   where it ends, at the vertex depended on
     */
    StrandMesh of(final EdgeStyle style, final Point3D start, final Point3D end) {
        final StrandMesh mesh = new StrandMesh();
        final double length = start.distance(end);
        if (!(length > 0)) {
            return mesh;
        }
        final Point3D along = end.subtract(start).normalize();
        final double radius = style.girth().radius();
        final double twins = radius * TWIN + (TWINS_APART + TWINS_APART_PER_RADIUS * radius) / 2;
        Point3D shaft = end;
        if (style.head() instanceof Head.Arrow) {
            final double head = Math.min(HEAD + HEAD_PER_RADIUS * radius, length * MOST_OF_IT_HEAD);
            final double width = HEAD_WIDTH + HEAD_WIDTH_PER_RADIUS * radius;
            shaft = end.subtract(along.multiply(head));
            mesh.cone(shaft, end, style.texture() instanceof Texture.Twin
                    ? Math.max(width, (twins + radius * TWIN) * HEAD_OVER_TWINS) : width);
        }
        final double reach = start.distance(shaft);
        switch (style.texture()) {
            case Texture.Solid() -> mesh.tube(start, shaft, radius);
            case Texture.Dashed() -> dashes(mesh, start, along, reach, radius);
            case Texture.Dotted() -> beads(mesh, start, along, reach, radius);
            case Texture.Twin() -> {
                final Point3D aside = aside(along).multiply(twins);
                mesh.tube(start.add(aside), shaft.add(aside), radius * TWIN);
                mesh.tube(start.subtract(aside), shaft.subtract(aside), radius * TWIN);
            }
        }
        return mesh;
    }

    /** Dashes from the start, the last cut short at the end if it must be. */
    private void dashes(final StrandMesh mesh, final Point3D start, final Point3D along, final double reach,
                        final double radius) {
        final double dash = DASH + DASH_PER_RADIUS * radius;
        final double every = dash + GAP + GAP_PER_RADIUS * radius;
        for (double from = 0; from < reach; from += every) {
            mesh.tube(start.add(along.multiply(from)), start.add(along.multiply(Math.min(from + dash, reach))),
                    radius);
        }
    }

    /** Beads from the start, evenly apart, none beyond the end; one in the middle if there is room for no more. */
    private void beads(final StrandMesh mesh, final Point3D start, final Point3D along, final double reach,
                       final double radius) {
        final double bead = BEAD + BEAD_PER_RADIUS * radius;
        if (reach < 2 * bead) {
            if (reach > 0) {
                mesh.bead(start.add(along.multiply(reach / 2)), Math.min(bead, reach / 2));
            }
            return;
        }
        final double apart = BEADS_APART + BEADS_APART_PER_RADIUS * radius;
        for (double at = bead; at <= reach - bead; at += apart) {
            mesh.bead(start.add(along.multiply(at)), bead);
        }
    }

    /**
     * Which way the twins lie apart: square to the edge and as near up as can be, so that they lie one above the
     * other; toward the front for an edge that runs up or down.
     */
    private Point3D aside(final Point3D along) {
        final Point3D up = new Point3D(0, -1, 0);
        final Point3D level = up.subtract(along.multiply(up.dotProduct(along)));
        if (level.magnitude() > LEVEL) {
            return level.normalize();
        }
        final Point3D front = new Point3D(0, 0, -1);
        return front.subtract(along.multiply(front.dotProduct(along))).normalize();
    }
}
