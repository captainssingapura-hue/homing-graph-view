package hue.captains.singapura.homing.graphview.cats;

import hue.captains.singapura.homing.graphview.layout.Layers;
import hue.captains.singapura.homing.graphview.model.Graph;
import hue.captains.singapura.homing.graphview.model.Vertex;
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

import static org.junit.jupiter.api.Assertions.*;

/** The cat breeds: the studbook as a graph in three generations, every breed explained, and every type's contract. */
class CatsTest {

    private final Graph breeds = new Studbook().graph();

    @Test
    void theStudbookIsAGraphOfFoundersAndTheBreedsBredFromThem() {
        assertEquals(35, breeds.vertices().size());
        final List<List<String>> generations = new Layers().of(breeds).stream()
                .map(layer -> layer.items().stream().map(Vertex::id).toList()).toList();
        assertEquals(List.of(12, 16, 7), generations.stream().map(List::size).toList());
        assertTrue(generations.get(0).containsAll(List.of("Persian", "Siamese", "Serval", "Asian Leopard Cat")));
        assertTrue(generations.get(1).containsAll(List.of("Himalayan", "Burmese", "Bengal", "Savannah")));
        assertTrue(generations.get(2).containsAll(List.of("Tonkinese", "Toyger", "Peterbald", "Oriental Longhair")));
    }

    @Test
    void everyBreedHasACoatAndADescription() {
        for (final Vertex vertex : breeds.vertices().items()) {
            final Breed breed = new Studbook().breed(vertex.id()).orElseThrow();
            assertFalse(breed.description().isBlank(), breed.name());
        }
        assertEquals(new Coat.Wild(), new Studbook().breed("Serval").orElseThrow().coat());
        assertEquals(new Coat.Hairless(), new Studbook().breed("Peterbald").orElseThrow().coat());
        assertTrue(new Studbook().breed("Tabby Mountain Lion").isEmpty());
    }

    @Test
    void aBreedIsExplainedByWhoItIsWhatItWasBredFromAndWhatCameOfIt() {
        final Explanation burmese = new CatExplanations().of(new Vertex("Burmese"), breeds);
        assertEquals("Burmese", burmese.heading());
        assertTrue(burmese.body().startsWith("Compact and glossy"), burmese.body());
        assertTrue(burmese.body().contains("Coat: short-haired."), burmese.body());
        assertTrue(burmese.body().contains("Bred from: Siamese."), burmese.body());
        assertTrue(burmese.body().contains("Gave rise to: Tonkinese, Bombay, Burmilla and Australian Mist."),
                burmese.body());
        assertTrue(new CatExplanations().of(new Vertex("Persian"), breeds).body()
                .contains("A founder: bred from no other breed here."));
    }

    @Test
    void theExampleRegistersTheBreedsAsBadgesAndPlainly() {
        final Registry registry = new Cats().registry(List.of());
        assertEquals(List.of("Cat breeds", "Cat breeds, plainly"),
                registry.items().stream().map(Exhibit::name).toList());
        assertEquals(new CatBadges(), registry.items().getFirst().shapes());
        assertEquals(new CatExplanations(), registry.items().getFirst().explanations());
        assertEquals(new Names(), registry.items().getLast().explanations());
    }

    @Test
    void everyTypeOfTheExampleKeepsItsContract() throws IOException {
        final CodeSource main = Cats.class.getProtectionDomain().getCodeSource();
        final List<Class<?>> types = new PackageScanner().scan(Cats.class.getPackageName()).stream()
                .filter(type -> main.equals(type.getProtectionDomain().getCodeSource()))
                .toList();
        assertTrue(types.containsAll(List.of(Cats.class, Studbook.class, Breed.class, Coat.class, CatBadges.class,
                CatExplanations.class)), "found " + types);
        final OntologyEnforcer enforcer = new OntologyEnforcer();
        final List<String> violations = types.stream()
                .flatMap(type -> enforcer.enforce(type).stream())
                .map(ContractViolation::message)
                .toList();
        assertEquals(List.of(), violations);
    }
}
