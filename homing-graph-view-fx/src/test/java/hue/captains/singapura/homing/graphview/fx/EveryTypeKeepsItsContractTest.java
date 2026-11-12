package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.enforcer.ContractViolation;
import hue.captains.singapura.tao.ontology.enforcer.OntologyEnforcer;
import hue.captains.singapura.tao.ontology.utils.PackageScanner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.CodeSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** jOntology's enforcer over every type of the view's main code: each keeps the contract it declares. */
class EveryTypeKeepsItsContractTest {

    @Test
    void everyTypeOfTheViewKeepsItsContract() throws IOException {
        final List<Class<?>> types = viewTypes();
        assertTrue(types.containsAll(List.of(GraphView.class, Orbit.class, Framing.class, CameraRig.class,
                Glide.class, Gestures.class, Slate.class, Rods.class, States.class, Palette.class, NodeState.class,
                EdgeState.class, FlyCamera.class, Velocity.class, Grounds.class, Grid.class, Ticker.class,
                FreeFlight.class, Navigator.class)), "found " + types);
        final OntologyEnforcer enforcer = new OntologyEnforcer();
        final List<String> violations = types.stream()
                .flatMap(type -> enforcer.enforce(type).stream())
                .map(ContractViolation::message)
                .toList();
        assertEquals(List.of(), violations);
    }

    /** The classes of the view's package that come from its main code, not from these tests. */
    private List<Class<?>> viewTypes() throws IOException {
        final CodeSource main = Orbit.class.getProtectionDomain().getCodeSource();
        return new PackageScanner().scan(Orbit.class.getPackageName()).stream()
                .filter(type -> main.equals(type.getProtectionDomain().getCodeSource()))
                .toList();
    }
}
