package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.homing.graphview.fx.Container;
import hue.captains.singapura.homing.graphview.fx.Containers;
import hue.captains.singapura.homing.graphview.fx.Uniform;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.samples.JdkModules;
import hue.captains.singapura.homing.graphview.samples.RoyalFamily;
import hue.captains.singapura.tao.ontology.Scalar;

/** A graph the demo can show: what the list calls it, the graph, how big its vertices are, and the window's title. */
sealed interface Exhibit extends Scalar {

    /** What the list of demos calls it. */
    String name();

    /** The graph. */
    Graph graph();

    /** Which container each vertex comes in. */
    Containers containers();

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
        public Containers containers() {
            return new PackageCounts();
        }

        @Override
        public String title(final Graph graph) {
            return (EVERY.equals(root) ? "every JDK module" : root + " and what it requires") + " - "
                    + graph.vertices().size() + " modules, " + graph.edges().size() + " requires";
        }
    }

    /** The British Royal Family, its generations as the layers, each person in a full container. */
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
        public Containers containers() {
            return new Uniform(new Container.Full());
        }

        @Override
        public String title(final Graph graph) {
            return "the British Royal Family - " + graph.vertices().size() + " people in four generations";
        }
    }
}
