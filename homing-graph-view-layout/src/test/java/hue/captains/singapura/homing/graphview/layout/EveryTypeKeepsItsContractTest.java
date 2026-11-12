package hue.captains.singapura.homing.graphview.layout;

import hue.captains.singapura.tao.ontology.enforcer.ContractViolation;
import hue.captains.singapura.tao.ontology.enforcer.OntologyEnforcer;
import hue.captains.singapura.tao.ontology.utils.PackageScanner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.CodeSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** jOntology's enforcer over every type of the layout's main code: each keeps the contract it declares. */
class EveryTypeKeepsItsContractTest {

    @Test
    void everyTypeOfTheLayoutKeepsItsContract() throws IOException {
        final List<Class<?>> types = layoutTypes();
        assertTrue(types.containsAll(List.of(Closure.class, Depths.class, LayersByDepth.class, Placed.class,
                Placement.class, Highlight.class, Focus.class, Reachability.class)), "found " + types);
        final OntologyEnforcer enforcer = new OntologyEnforcer();
        final List<String> violations = types.stream()
                .flatMap(type -> enforcer.enforce(type).stream())
                .map(ContractViolation::message)
                .toList();
        assertEquals(List.of(), violations);
    }

    /** The classes of the layout's package that come from its main code, not from these tests. */
    private List<Class<?>> layoutTypes() throws IOException {
        final CodeSource main = Placement.class.getProtectionDomain().getCodeSource();
        return new PackageScanner().scan(Placement.class.getPackageName()).stream()
                .filter(type -> main.equals(type.getProtectionDomain().getCodeSource()))
                .toList();
    }
}
