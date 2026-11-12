package hue.captains.singapura.homing.graphview.bench;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import javafx.scene.paint.Color;

/** The bench's look, the 3D view's palette laid flat: slates on a dark sheet, the block a shade lighter. */
record Colours() implements StatelessFunctionalObject {

    /** Behind everything. */
    Color sheet() {
        return Color.web("#14171c");
    }

    /** The block's face. */
    Color block() {
        return Color.web("#1a1e25");
    }

    /** The block's edge. */
    Color blockEdge() {
        return Color.web("#2c323c");
    }

    /** An empty slot's outline. */
    Color slot() {
        return Color.web("#2a3039");
    }

    /** Captions and the readout. */
    Color caption() {
        return Color.web("#8a94a6");
    }

    /** A slate's face. */
    Color slate() {
        return Color.web("#3d4b5f");
    }

    /** A slate's face under the mouse. */
    Color slateHovered() {
        return Color.web("#2f7de1");
    }

    /** A slate's text. */
    Color text() {
        return Color.web("#eef2f7");
    }
}
