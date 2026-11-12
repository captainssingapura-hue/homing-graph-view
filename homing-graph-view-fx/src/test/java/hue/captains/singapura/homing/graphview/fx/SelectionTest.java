package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.model.Vertex;
import javafx.util.Subscription;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The selection event, some vertex or nothing, and the feed that tells every listener of each change. */
class SelectionTest {

    private final Vertex app = new Vertex("app");
    private final Vertex core = new Vertex("core");

    @Test
    void theEventIsSomeVertexOrNothingAndEqualByValue() {
        assertEquals(new VertexSelection.Some(app), new VertexSelection.Some(new Vertex("app")));
        assertNotEquals(new VertexSelection.Some(app), new VertexSelection.Nothing());
        assertThrows(NullPointerException.class, () -> new VertexSelection.Some(null));
        // a listener can switch on the two and clear as easily as it shows
        final VertexSelection event = new VertexSelection.Nothing();
        final String shown = switch (event) {
            case VertexSelection.Some(Vertex vertex) -> vertex.id();
            case VertexSelection.Nothing() -> "";
        };
        assertEquals("", shown);
    }

    @Test
    void everyListenerHearsEachChangeInTurnAndNothingIsSelectedToBeginWith() {
        final SelectionFeed feed = new SelectionFeed();
        assertEquals(new VertexSelection.Nothing(), feed.current());
        final List<String> heard = new ArrayList<>();
        feed.subscribe(event -> heard.add("first " + event));
        feed.subscribe(event -> heard.add("second " + event));
        feed.emit(new VertexSelection.Some(app));
        assertEquals(List.of("first " + new VertexSelection.Some(app), "second " + new VertexSelection.Some(app)),
                heard);
        assertEquals(new VertexSelection.Some(app), feed.current());
    }

    @Test
    void settingTheSelectionToWhatItIsTellsNoOne() {
        final SelectionFeed feed = new SelectionFeed();
        final List<VertexSelection> heard = new ArrayList<>();
        feed.subscribe(heard::add);
        feed.emit(new VertexSelection.Nothing());
        feed.emit(new VertexSelection.Some(app));
        feed.emit(new VertexSelection.Some(app));
        feed.emit(new VertexSelection.Some(core));
        feed.emit(new VertexSelection.Nothing());
        feed.emit(new VertexSelection.Nothing());
        assertEquals(List.of(new VertexSelection.Some(app), new VertexSelection.Some(core),
                new VertexSelection.Nothing()), heard);
    }

    @Test
    void aListenerThatUnsubscribesHearsNoMoreEvenFromWithinItsOwnTurn() {
        final SelectionFeed feed = new SelectionFeed();
        final List<VertexSelection> heard = new ArrayList<>();
        final Subscription[] subscription = new Subscription[1];
        subscription[0] = feed.subscribe(event -> {
            heard.add(event);
            subscription[0].unsubscribe();
        });
        feed.emit(new VertexSelection.Some(app));
        feed.emit(new VertexSelection.Some(core));
        assertEquals(List.of(new VertexSelection.Some(app)), heard);
        assertThrows(NullPointerException.class, () -> feed.subscribe(null));
    }
}
