package hue.captains.singapura.homing.graphview.viewer;

import hue.captains.singapura.homing.graphview.fx.Colour;
import hue.captains.singapura.homing.graphview.fx.Container;
import hue.captains.singapura.homing.graphview.fx.ContainerShapes;
import hue.captains.singapura.homing.graphview.fx.EdgeStyle;
import hue.captains.singapura.homing.graphview.fx.Order;
import hue.captains.singapura.homing.graphview.fx.SameStyle;
import hue.captains.singapura.homing.graphview.fx.Stance;
import hue.captains.singapura.homing.graphview.fx.Uniform;
import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Edges;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.model.Vertices;
import hue.captains.singapura.tao.ontology.enforcer.ContractViolation;
import hue.captains.singapura.tao.ontology.enforcer.OntologyEnforcer;
import hue.captains.singapura.tao.ontology.utils.PackageScanner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.CodeSource;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** The viewer's registry of exhibits, what it explains by default, its arguments, and every type's contract. */
class ViewerTest {

    private final Vertex app = new Vertex("app");
    private final Vertex core = new Vertex("core");
    private final Graph graph = new Graph(new Vertices(List.of(app, core)), new Edges(List.of(new Edge(app, core))));
    private final Registry registry = new Registry(List.of(new Exhibit("Apps and cores", graph),
            new Exhibit("British Royal Family", graph), new Exhibit("JDK: every module (ALL-SYSTEM)", graph)));

    @Test
    void anExhibitShownPlainlyIsInFullContainersExplainedByNameWithPlainRods() {
        final Exhibit plain = new Exhibit("Plain", graph);
        assertEquals(new ContainerShapes(new Uniform(new Container.Full())), plain.shapes());
        assertEquals(new Names(), plain.explanations());
        assertEquals(new Explanation("core", ""), plain.explanations().of(core, graph));
        assertEquals(new SameStyle(), plain.styler());
        final SameStyle gold = new SameStyle(new EdgeStyle().with(new Colour(0xd4af37)));
        assertEquals(new Exhibit("Plain", graph, plain.shapes(), plain.explanations(), gold), plain.with(gold));
        assertThrows(NullPointerException.class, () -> plain.with(null));
        assertThrows(IllegalArgumentException.class, () -> new Exhibit(" ", graph));
        assertThrows(NullPointerException.class, () -> new Exhibit("No graph", null));
    }

    @Test
    void aRegistryKeepsItsOrderRefusesTwoOfOneNameAndFindsByPartOfAName() {
        assertEquals(List.of("Apps and cores", "British Royal Family", "JDK: every module (ALL-SYSTEM)"),
                registry.items().stream().map(Exhibit::name).toList());
        assertEquals(Optional.of(1), registry.find("royal"));
        assertEquals(Optional.of(2), registry.find("all-system"));
        assertEquals(Optional.empty(), registry.find("cats"));
        assertThrows(IllegalArgumentException.class, () -> new Registry(List.of(new Exhibit("Twice", graph),
                new Exhibit("Twice", graph))));
        assertThrows(IllegalArgumentException.class, () -> new Registry(List.of()));
    }

    @Test
    void theArgumentsSayHowToStageAndWhereToStart() {
        final Arguments arguments = new Arguments();
        assertEquals(new Stance.Standing(), arguments.staging(List.of()).stance());
        assertEquals(new Order.BaseFar(), arguments.staging(List.of()).order());
        assertEquals(new Stance.Lying(), arguments.staging(List.of("royal", "lying", "near")).stance());
        assertEquals(new Order.BaseNear(), arguments.staging(List.of("near", "royal", "lying")).order());
        assertEquals(Optional.of("royal"), arguments.asked(List.of("lying", "royal", "near")));
        assertEquals(1, arguments.first(List.of("lying", "royal"), registry));
        assertEquals(0, arguments.first(List.of("lying"), registry), "nothing asked: the first");
        assertEquals(0, arguments.first(List.of("cats"), registry), "nothing found: the first");
    }

    @Test
    void everyTypeOfTheViewerKeepsItsContract() throws IOException {
        final CodeSource main = Viewer.class.getProtectionDomain().getCodeSource();
        final List<Class<?>> types = new PackageScanner().scan(Viewer.class.getPackageName()).stream()
                .filter(type -> main.equals(type.getProtectionDomain().getCodeSource()))
                .toList();
        assertTrue(types.containsAll(List.of(Viewer.class, ViewerApplication.class, Registry.class, Exhibit.class,
                Explanations.class, Explanation.class, Names.class, Arguments.class, Details.class)), "found " + types);
        final OntologyEnforcer enforcer = new OntologyEnforcer();
        final List<String> violations = types.stream()
                .flatMap(type -> enforcer.enforce(type).stream())
                .map(ContractViolation::message)
                .toList();
        assertEquals(List.of(), violations);
    }
}
