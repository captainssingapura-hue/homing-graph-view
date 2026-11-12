package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.homing.graphview.fx.ContainerShapes;
import hue.captains.singapura.homing.graphview.fx.Room;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
import hue.captains.singapura.homing.graphview.samples.RoyalFamily;
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

/** The demo's exhibits: the royal family framed by title, the JDK's modules in containers, and every type's contract. */
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
    void theRoyalFamilyIsFramedAndTheModulesAreContainers() {
        assertEquals(new Portraits(), new Exhibit.Royals().shapes());
        assertEquals(new Room(140, 172, 14), new Portraits().room());
        assertEquals(new ContainerShapes(new PackageCounts()), new Exhibit.Modules("java.se").shapes());
        assertEquals("British Royal Family", new Exhibit.Royals().name());
        assertEquals("Every JDK module", new Exhibit.Modules("ALL-SYSTEM").name());
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
        final Detail william = new Exhibit.Royals().detail(new Vertex("Prince William"), family);
        assertEquals("Prince William", william.heading());
        assertTrue(william.body().startsWith("Prince of Wales") && william.body().contains("\n\nParents: "));
    }

    @Test
    void theDetailsOfAModuleAreJustItsName() {
        final Graph modules = new Exhibit.Modules("java.se").graph();
        assertEquals(new Detail("java.base", ""), new Exhibit.Modules("java.se").detail(new Vertex("java.base"),
                modules));
    }

    @Test
    void everyTypeOfTheDemoKeepsItsContract() throws IOException {
        final CodeSource main = Demo.class.getProtectionDomain().getCodeSource();
        final List<Class<?>> types = new PackageScanner().scan(Demo.class.getPackageName()).stream()
                .filter(type -> main.equals(type.getProtectionDomain().getCodeSource()))
                .toList();
        assertTrue(types.containsAll(List.of(Demo.class, Showcase.class, Exhibit.class, PackageCounts.class,
                Portraits.class, Placeholders.class, Titles.class, Title.class, Details.class, Detail.class,
                Biographies.class, Kin.class)), "found " + types);
        final OntologyEnforcer enforcer = new OntologyEnforcer();
        final List<String> violations = types.stream()
                .flatMap(type -> enforcer.enforce(type).stream())
                .map(ContractViolation::message)
                .toList();
        assertEquals(List.of(), violations);
    }
}
