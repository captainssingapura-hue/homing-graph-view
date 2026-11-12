package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Scalar;
import javafx.geometry.Point3D;

/**
 * Where a layer's platform is: the floor its containers sit on when lying, the board they hang on when standing.
 *
 * @param depth  the layer, by depth
 * @param x      its centre's x
 * @param y      its centre's y
 * @param z      its centre's z
 * @param width  its size along x
 * @param height its size along y
 * @param deep   its size along z
 */
record Deck(int depth, double x, double y, double z, double width, double height, double deep) implements Scalar {

    /** A layer's platform, centred on a point, of a size along x, y and z. */
    Deck(final int depth, final Point3D centre, final Point3D size) {
        this(depth, centre.getX(), centre.getY(), centre.getZ(), size.getX(), size.getY(), size.getZ());
    }

    /** Its centre. */
    Point3D centre() {
        return new Point3D(x, y, z);
    }

    /** Its size along x, y and z. */
    Point3D size() {
        return new Point3D(width, height, deep);
    }

    /** Its lowest corner. */
    Point3D low() {
        return centre().subtract(size().multiply(0.5));
    }

    /** Its highest corner. */
    Point3D high() {
        return centre().add(size().multiply(0.5));
    }
}
