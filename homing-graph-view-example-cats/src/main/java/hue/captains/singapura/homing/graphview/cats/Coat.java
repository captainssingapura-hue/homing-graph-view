package hue.captains.singapura.homing.graphview.cats;

import hue.captains.singapura.tao.ontology.Scalar;

/** A breed's coat, which colours its badge: long, short, none at all, or a wild cat's. */
sealed interface Coat extends Scalar {

    /** Long-haired. */
    record Long() implements Coat { }

    /** Short-haired. */
    record Short() implements Coat { }

    /** Hairless, or nearly so. */
    record Hairless() implements Coat { }

    /** A wild cat, not a domestic breed. */
    record Wild() implements Coat { }
}
