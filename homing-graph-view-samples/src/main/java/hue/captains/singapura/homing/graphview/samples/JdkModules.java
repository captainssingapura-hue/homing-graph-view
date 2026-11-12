package hue.captains.singapura.homing.graphview.samples;

import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Edges;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.model.Vertices;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The running JDK's modules as a graph: a vertex for each module, named by its name, and an edge for each module it
 * requires, from the module to the one required. Compile-time-only requirements (requires static) are left out.
 */
public record JdkModules() implements StatelessFunctionalObject {

    /** Asks for every module of the JDK rather than one and what it requires, as java --add-modules does. */
    private static final String EVERY = "ALL-SYSTEM";

    /**
     * A module and every module it requires, directly or not; or every module of the JDK.
     *
     * @param root a system module's name, such as java.se, or ALL-SYSTEM
     */
    public Graph of(final String root) {
        final Map<String, ModuleDescriptor> reached = reached(root);
        final Map<String, Vertex> vertices = new TreeMap<>();
        reached.keySet().forEach(name -> vertices.put(name, new Vertex(name)));
        final List<Edge> edges = new ArrayList<>();
        reached.forEach((name, descriptor) -> required(descriptor)
                .forEach(other -> edges.add(new Edge(vertices.get(name), vertices.get(other)))));
        return new Graph(new Vertices(List.copyOf(vertices.values())), new Edges(edges));
    }

    /** The modules reached from the root, by name. */
    private Map<String, ModuleDescriptor> reached(final String root) {
        final ModuleFinder finder = ModuleFinder.ofSystem();
        final Deque<String> pending = new ArrayDeque<>(EVERY.equals(root)
                ? finder.findAll().stream().map(module -> module.descriptor().name()).toList()
                : List.of(root));
        final Map<String, ModuleDescriptor> reached = new TreeMap<>();
        while (!pending.isEmpty()) {
            final String name = pending.pop();
            if (!reached.containsKey(name)) {
                final ModuleDescriptor descriptor = finder.find(name).map(ModuleReference::descriptor)
                        .orElseThrow(() -> new IllegalArgumentException("This JDK has no module called " + name));
                reached.put(name, descriptor);
                required(descriptor).forEach(pending::push);
            }
        }
        return reached;
    }

    /** The modules a module requires at run time, by name, in order. */
    private List<String> required(final ModuleDescriptor descriptor) {
        return descriptor.requires().stream()
                .filter(requires -> !requires.modifiers().contains(ModuleDescriptor.Requires.Modifier.STATIC))
                .map(ModuleDescriptor.Requires::name)
                .sorted()
                .toList();
    }
}
