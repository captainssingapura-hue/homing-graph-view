package hue.captains.singapura.homing.graphview.bench;

import hue.captains.singapura.homing.graphview.layout.Arrangement;
import hue.captains.singapura.homing.graphview.layout.Ladder;
import hue.captains.singapura.homing.graphview.layout.LayerInBlock;
import hue.captains.singapura.homing.graphview.layout.Layers;
import hue.captains.singapura.homing.graphview.model.Vertices;
import hue.captains.singapura.tao.ontology.enforcer.ContractViolation;
import hue.captains.singapura.tao.ontology.enforcer.OntologyEnforcer;
import hue.captains.singapura.tao.ontology.utils.PackageScanner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.CodeSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The bench's text: what a line names, what the picker and the readout say, and every type keeping its contract. */
class BenchTest {

    private final Ladder monitor = new Ladder(Drawing.SLOT_WIDTH / Drawing.SLOT_HEIGHT, 16.0 / 9, 1.5);

    @Test
    void numbersNameAMadeUpGraphAndAnythingElseAJdkModule() {
        assertEquals(2 + 8 + 30, new Sources().of(" 2 8, 30 ").vertices().size());
        assertTrue(new Sources().of("java.se").vertices().size() > 10);
        assertThrows(IllegalArgumentException.class, () -> new Sources().of("no.such.module"));
    }

    @Test
    void thePickerNamesEachLayerAndTheReadoutItsBlockAndTheLadderToIt() {
        final List<Vertices> layers = new Layers().of(new Sources().of("2 8 30 9"));
        assertEquals("depth 2  ·  30 vertices", new Readout().name(2, layers.get(2)));
        final Arrangement arrangement = new LayerInBlock(monitor).of(layers.get(2));
        final String text = new Readout().of(layers, 2, arrangement, monitor);
        assertTrue(text.startsWith("depth 2 of 4 layers, 30 vertices"), text);
        assertTrue(text.contains("block 5×6, the ladder's 5×8 cut to the rows filled"), text);
        assertTrue(text.endsWith("ladder 1×1 → 1×2 → 2×3 → 3×5 → 4×6 → 5×8"), text);
    }

    @Test
    void everyTypeOfTheBenchKeepsItsContract() throws IOException {
        final CodeSource main = Bench.class.getProtectionDomain().getCodeSource();
        final List<Class<?>> types = new PackageScanner().scan(Bench.class.getPackageName()).stream()
                .filter(type -> main.equals(type.getProtectionDomain().getCodeSource()))
                .toList();
        assertTrue(types.containsAll(List.of(Bench.class, Workbench.class, Sheet.class, Drawing.class, Card.class,
                Colours.class, Sources.class, Readout.class)), "found " + types);
        final OntologyEnforcer enforcer = new OntologyEnforcer();
        final List<String> violations = types.stream()
                .flatMap(type -> enforcer.enforce(type).stream())
                .map(ContractViolation::message)
                .toList();
        assertEquals(List.of(), violations);
    }
}
