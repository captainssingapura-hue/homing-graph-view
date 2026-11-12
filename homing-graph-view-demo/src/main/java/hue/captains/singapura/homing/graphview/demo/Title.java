package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.tao.ontology.Scalar;

/** A royal title, as a person's name begins with it, which picks their placeholder picture; or none. */
sealed interface Title extends Scalar {

    /** A king. */
    record King() implements Title { }

    /** A queen. */
    record Queen() implements Title { }

    /** A prince. */
    record Prince() implements Title { }

    /** A princess. */
    record Princess() implements Title { }

    /** No royal title in the name. */
    record Untitled() implements Title { }
}
