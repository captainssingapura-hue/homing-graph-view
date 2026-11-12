package hue.captains.singapura.homing.graphview.samples;

import hue.captains.singapura.homing.graphview.layout.Ladder;
import hue.captains.singapura.homing.graphview.layout.Layers;
import hue.captains.singapura.homing.graphview.layout.LayersByDepth;
import hue.captains.singapura.homing.graphview.layout.Placed;
import hue.captains.singapura.homing.graphview.layout.Placement;
import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.tao.ontology.enforcer.ContractViolation;
import hue.captains.singapura.tao.ontology.enforcer.OntologyEnforcer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/** The sample graphs: a made-up graph has the layers asked for, the JDK's modules are read whole, and the royal family
 * comes in its generations. */
class SamplesTest {

    @Test
    void aMadeUpGraphHasTheLayersAskedFor() {
        final List<Integer> sizes = List.of(4, 1, 9, 30, 2);
        final Graph graph = new MadeUp().of(sizes);
        final Placement placement = new LayersByDepth(new Ladder(230.0 / 80, 16.0 / 9, 1.5)).of(graph);
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
        final Placement placement = new LayersByDepth(new Ladder(230.0 / 80, 16.0 / 9, 1.5)).of(graph);
        assertEquals(0, placement.of(new Vertex("java.base")).map(Placed::depth).orElseThrow());
        assertEquals(placement.maxDepth(), placement.of(new Vertex("java.se")).map(Placed::depth).orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> new JdkModules().of("no.such.module"));
    }

    @Test
    void theRoyalFamilysLayersAreItsFourGenerations() {
        final Graph family = new RoyalFamily().of();
        assertEquals(31, family.vertices().size());
        final List<List<String>> generations = new Layers().of(family).stream()
                .map(layer -> layer.items().stream().map(Vertex::id).toList()).toList();
        assertEquals(List.of(2, 6, 8, 15), generations.stream().map(List::size).toList());
        assertEquals(List.of("Queen Elizabeth II", "Prince Philip"), generations.get(0));
        // those who married in share their spouse's generation, through the marriage's two edges
        assertEquals(List.of("King Charles III", "Diana Spencer", "Queen Camilla", "Princess Anne",
                "Andrew Mountbatten-Windsor", "Prince Edward"), generations.get(1));
        assertTrue(generations.get(2).containsAll(List.of("Prince William", "Zara Tindall", "James, Earl of Wessex")));
        assertTrue(generations.get(3).containsAll(List.of("Prince George", "Princess Lilibet", "Adelaide")));
        assertTrue(family.edges().items().contains(new Edge(new Vertex("Prince William"),
                new Vertex("Diana Spencer"))), "a child points to each parent");
    }

    @Test
    void everySampleKeepsItsContract() {
        final OntologyEnforcer enforcer = new OntologyEnforcer();
        final List<String> violations = List.of(JdkModules.class, MadeUp.class, RoyalFamily.class).stream()
                .flatMap(type -> enforcer.enforce(type).stream())
                .map(ContractViolation::message)
                .toList();
        assertEquals(List.of(), violations);
    }
}
