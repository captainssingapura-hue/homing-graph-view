package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.homing.graphview.fx.ContainerShapes;
import hue.captains.singapura.homing.graphview.fx.Room;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.samples.RoyalFamily;
import hue.captains.singapura.homing.graphview.viewer.Exhibit;
import hue.captains.singapura.homing.graphview.viewer.Explanation;
import hue.captains.singapura.homing.graphview.viewer.Names;
import hue.captains.singapura.homing.graphview.viewer.Registry;
import hue.captains.singapura.tao.ontology.enforcer.ContractViolation;
import hue.captains.singapura.tao.ontology.enforcer.OntologyEnforcer;
import hue.captains.singapura.tao.ontology.utils.PackageScanner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.CodeSource;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The demo's registry: the JDK's modules in containers, the royal family framed by title, and every type's contract.
 */
class DemoTest {

    @Test
    void aNameBeginningWithATitleHasThatTitle() {
        final Titles titles = new Titles();
        assertEquals(new Title.King(), titles.of("King Charles III"));
        assertEquals(new Title.Queen(), titles.of("Queen Camilla"));
        assertEquals(new Title.Prince(), titles.of("Prince George"));
        assertEquals(new Title.Princess(), titles.of("Princess Charlotte"));
        assertEquals(new Title.Untitled(), titles.of("Diana Spencer"));
        assertEquals(new Title.Untitled(), titles.of("James, Earl of Wessex"));
        assertEquals(new Title.Untitled(), titles.of("Kingsley"), "a whole word, not a beginning");
    }

    @Test
    void theRoyalFamilyHasAKingTwoQueensSevenPrincesAndFivePrincesses() {
        final Map<String, Integer> counted = new TreeMap<>();
        new RoyalFamily().of().vertices().items().stream().map(Vertex::id)
                .forEach(name -> counted.merge(new Titles().of(name).getClass().getSimpleName(), 1, Integer::sum));
        assertEquals(Map.of("King", 1, "Queen", 2, "Prince", 7, "Princess", 5, "Untitled", 16), counted);
    }

    @Test
    void theRegistryHoldsTheModulesInContainersAndTheRoyalFamilyInPortraits() {
        final Registry registry = new Demo().registry(List.of());
        assertEquals(List.of("JDK: java.se", "JDK: java.desktop", "JDK: jdk.jshell", "JDK: every module (ALL-SYSTEM)",
                "British Royal Family"), registry.items().stream().map(Exhibit::name).toList());
        final Exhibit royals = registry.items().getLast();
        assertEquals(new Portraits(), royals.shapes());
        assertEquals(new RoyalExplanations(), royals.explanations());
        assertEquals(new Room(140, 172, 14), new Portraits().room());
        assertEquals(new ContainerShapes(new PackageCounts()), registry.items().getFirst().shapes());
        assertEquals(new Names(), registry.items().getFirst().explanations());
    }

    @Test
    void aModuleAskedForThatIsNotListedJoinsAtTheTopAndAnythingListedIsFound() {
        final Registry withXml = new Demo().registry(List.of("java.xml", "lying"));
        assertEquals("JDK: java.xml", withXml.items().getFirst().name());
        assertEquals(6, withXml.size());
        assertEquals(5, new Demo().registry(List.of("royal", "near")).size());
        assertEquals(5, new Demo().registry(List.of("no.such.module")).size(), "a module the JDK lacks is left out");
    }

    @Test
    void everyoneInTheRoyalFamilyHasAShortDescription() {
        new RoyalFamily().of().vertices().items().forEach(person ->
                assertFalse(new Biographies().of(person.id()).isBlank(), person.id() + " has no description"));
        assertEquals("", new Biographies().of("Nobody"));
        assertTrue(new Biographies().of("King Charles III").startsWith("King of the United Kingdom since 2022"));
    }

    @Test
    void theDetailsOfAPersonNameTheirParentsSpousesAndChildrenFromTheGraph() {
        final Graph family = new RoyalFamily().of();
        assertEquals("Parents: King Charles III and Diana Spencer\n"
                + "Children: Prince George, Princess Charlotte and Prince Louis",
                new Kin().of(family, new Vertex("Prince William")));
        assertEquals("Parents: Queen Elizabeth II and Prince Philip\nMarried to: Diana Spencer and Queen Camilla\n"
                + "Children: Prince William and Prince Harry", new Kin().of(family, new Vertex("King Charles III")));
        assertEquals("Parent: Princess Eugenie", new Kin().of(family, new Vertex("Adelaide")));
        final Explanation william = new RoyalExplanations().of(new Vertex("Prince William"), family);
        assertEquals("Prince William", william.heading());
        assertTrue(william.body().startsWith("Prince of Wales") && william.body().contains("\n\nParents: "));
    }

    @Test
    void theDetailsOfAModuleAreJustItsName() {
        final Exhibit javaSe = new Demo().registry(List.of()).items().getFirst();
        assertEquals(new Explanation("java.base", ""), javaSe.explanations().of(new Vertex("java.base"),
                javaSe.graph()));
    }

    @Test
    void everyTypeOfTheDemoKeepsItsContract() throws IOException {
        final CodeSource main = Demo.class.getProtectionDomain().getCodeSource();
        final List<Class<?>> types = new PackageScanner().scan(Demo.class.getPackageName()).stream()
                .filter(type -> main.equals(type.getProtectionDomain().getCodeSource()))
                .toList();
        assertTrue(types.containsAll(List.of(Demo.class, PackageCounts.class, Portraits.class, Placeholders.class,
                Titles.class, Title.class, Biographies.class, Kin.class, RoyalExplanations.class)), "found " + types);
        final OntologyEnforcer enforcer = new OntologyEnforcer();
        final List<String> violations = types.stream()
                .flatMap(type -> enforcer.enforce(type).stream())
                .map(ContractViolation::message)
                .toList();
        assertEquals(List.of(), violations);
    }
}
