package hue.captains.singapura.homing.graphview.samples;

import hue.captains.singapura.homing.graphview.layout.LayersByDepth;
import hue.captains.singapura.homing.graphview.layout.Placed;
import hue.captains.singapura.homing.graphview.layout.Placement;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.enforcer.ContractViolation;
import hue.captains.singapura.tao.ontology.enforcer.OntologyEnforcer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/** The sample graphs: a made-up graph has the layers asked for, and the JDK's modules are read whole. */
class SamplesTest {

    @Test
    void aMadeUpGraphHasTheLayersAskedFor() {
        final List<Integer> sizes = List.of(4, 1, 9, 30, 2);
        final Graph graph = new MadeUp().of(sizes);
        final Placement placement = new LayersByDepth().of(graph);
        final Map<Integer, Integer> counted = new TreeMap<>();
        placement.items().forEach(placed -> counted.merge(placed.depth(), 1, Integer::sum));
        assertEquals(List.of(4, 1, 9, 30, 2), List.copyOf(counted.values()));
        assertEquals("L3.07", graph.vertices().items().get(4 + 1 + 9 + 7).id());
        assertEquals(graph, new MadeUp().of(sizes), "the same every time");
    }

    @Test
    void aMadeUpLayerWithoutVerticesIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new MadeUp().of(List.of(3, 0, 2)));
        assertThrows(IllegalArgumentException.class, () -> new MadeUp().of(List.of()));
    }

    @Test
    void javaSeIsItselfAndEverythingItRequires() {
        final Graph graph = new JdkModules().of("java.se");
        final List<String> ids = graph.vertices().items().stream().map(Vertex::id).toList();
        assertTrue(ids.containsAll(List.of("java.se", "java.base", "java.desktop")), ids.toString());
        final Placement placement = new LayersByDepth().of(graph);
        assertEquals(0, placement.of(new Vertex("java.base")).map(Placed::depth).orElseThrow());
        assertEquals(placement.maxDepth(), placement.of(new Vertex("java.se")).map(Placed::depth).orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> new JdkModules().of("no.such.module"));
    }

    @Test
    void everySampleKeepsItsContract() {
        final OntologyEnforcer enforcer = new OntologyEnforcer();
        final List<String> violations = List.of(JdkModules.class, MadeUp.class).stream()
                .flatMap(type -> enforcer.enforce(type).stream())
                .map(ContractViolation::message)
                .toList();
        assertEquals(List.of(), violations);
    }
}
