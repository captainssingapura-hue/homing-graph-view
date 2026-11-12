package hue.captains.singapura.homing.graphview.model;

import hue.captains.singapura.tao.ontology.enforcer.ContractViolation;
import hue.captains.singapura.tao.ontology.enforcer.OntologyEnforcer;
import hue.captains.singapura.tao.ontology.utils.PackageScanner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.CodeSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** jOntology's enforcer over every type of the model's main code: each keeps the contract it declares. */
class EveryTypeKeepsItsContractTest {

    @Test
    void everyTypeOfTheModelKeepsItsContract() throws IOException {
        final List<Class<?>> types = modelTypes();
        assertTrue(types.containsAll(List.of(Vertex.class, Edge.class, Vertices.class, Edges.class, Graph.class)),
                "found " + types);
        final OntologyEnforcer enforcer = new OntologyEnforcer();
        final List<String> violations = types.stream()
                .flatMap(type -> enforcer.enforce(type).stream())
                .map(ContractViolation::message)
                .toList();
        assertEquals(List.of(), violations);
    }

    /** The classes of the model's package that come from its main code, not from these tests. */
    private List<Class<?>> modelTypes() throws IOException {
        final CodeSource main = Graph.class.getProtectionDomain().getCodeSource();
        return new PackageScanner().scan(Graph.class.getPackageName()).stream()
                .filter(type -> main.equals(type.getProtectionDomain().getCodeSource()))
                .toList();
    }
}
