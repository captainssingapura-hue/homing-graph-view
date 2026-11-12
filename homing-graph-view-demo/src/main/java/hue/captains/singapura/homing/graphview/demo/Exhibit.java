package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.homing.graphview.fx.ContainerShapes;
import hue.captains.singapura.homing.graphview.fx.Shapes;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.samples.JdkModules;
import hue.captains.singapura.homing.graphview.samples.RoyalFamily;
import hue.captains.singapura.tao.ontology.Scalar;

/**
 * A graph the demo can show: what the list calls it, the graph, what its vertices are drawn as, and the window's title.
 */
sealed interface Exhibit extends Scalar {

    /** What the list of demos calls it. */
    String name();

    /** The graph. */
    Graph graph();

    /** What each vertex is drawn as. */
    Shapes shapes();

    /** What the window says of the graph. */
    String title(Graph graph);

    /**
     * A JDK module and every module it requires, each a container sized by its packages; or every module.
     *
     * @param root a system module's name, or ALL-SYSTEM
     */
    record Modules(String root) implements Exhibit {

        private static final String EVERY = "ALL-SYSTEM";

        @Override
        public String name() {
            return EVERY.equals(root) ? "Every JDK module" : "JDK: " + root;
        }

        @Override
        public Graph graph() {
            return new JdkModules().of(root);
        }

        @Override
        public Shapes shapes() {
            return new ContainerShapes(new PackageCounts());
        }

        @Override
        public String title(final Graph graph) {
            return (EVERY.equals(root) ? "every JDK module" : root + " and what it requires") + " - "
                    + graph.vertices().size() + " modules, " + graph.edges().size() + " requires";
        }
    }

    /** The British Royal Family, its generations as the layers, each person a portrait in a frame. */
    record Royals() implements Exhibit {

        @Override
        public String name() {
            return "British Royal Family";
        }

        @Override
        public Graph graph() {
            return new RoyalFamily().of();
        }

        @Override
        public Shapes shapes() {
            return new Portraits();
        }

        @Override
        public String title(final Graph graph) {
            return "the British Royal Family - " + graph.vertices().size() + " people in four generations";
        }
    }
}
