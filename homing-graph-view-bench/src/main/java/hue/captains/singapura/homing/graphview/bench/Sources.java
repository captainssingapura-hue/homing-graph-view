package hue.captains.singapura.homing.graphview.bench;

import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.samples.JdkModules;
import hue.captains.singapura.homing.graphview.samples.MadeUp;
import hue.captains.singapura.homing.graphview.samples.RoyalFamily;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import java.util.Arrays;
import java.util.List;

/**
 * The graph a line of text names: layer sizes, as {@code 2 8 30 90}, for a made-up graph with those layers, base first;
 * royal for the British Royal Family; otherwise a JDK module and everything it requires, or ALL-SYSTEM for every
 * module.
 */
record Sources() implements StatelessFunctionalObject {

    /**
     * The graph named.
     *
     * @param text layer sizes, royal, a JDK module's name, or ALL-SYSTEM
     */
    Graph of(final String text) {
        final String trimmed = text.strip();
        if (trimmed.matches("[0-9][0-9 ,]*")) {
            final List<Integer> sizes = Arrays.stream(trimmed.split("[ ,]+")).map(Integer::valueOf).toList();
            return new MadeUp().of(sizes);
        }
        if ("royal".equalsIgnoreCase(trimmed)) {
            return new RoyalFamily().of();
        }
        return new JdkModules().of(trimmed);
    }
}
