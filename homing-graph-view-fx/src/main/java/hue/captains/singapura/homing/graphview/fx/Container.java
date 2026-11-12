package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Scalar;

/**
 * The standard sizes a vertex's container comes in, as shipping containers do: small, half and full, by length, all of
 * one height and one width. Whatever its size, a container takes one slot of its layer's block: the layout gives every
 * vertex the same room, and how big a vertex is shows only in the container drawn there.
 */
public sealed interface Container extends Scalar {

    /** Its length, as a share of a full container's. */
    double length();

    /** A quarter of a full container's length. */
    record Small() implements Container {
        @Override
        public double length() {
            return 0.25;
        }
    }

    /** Half a full container's length. */
    record Half() implements Container {
        @Override
        public double length() {
            return 0.5;
        }
    }

    /** As long as a slot allows. */
    record Full() implements Container {
        @Override
        public double length() {
            return 1;
        }
    }
}
