package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.tao.ontology.Mutable;
import javafx.util.Subscription;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Where a view's selection events go out: to every listener subscribed, in the order they subscribed, each time the
 * selection changes, and not when it is set to what it already was. Nothing is selected to begin with.
 */
final class SelectionFeed implements Mutable {

    private final List<Consumer<? super VertexSelection>> listeners = new ArrayList<>();
    private VertexSelection current = new VertexSelection.Nothing();

    /**
     * Adds a listener for every change from now on.
     *
     * @param listener told of each change
     * @return what ends the listening
     */
    Subscription subscribe(final Consumer<? super VertexSelection> listener) {
        Objects.requireNonNull(listener, "A selection listener cannot be missing");
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    /** Tells every listener of the selection, if it is a change. */
    void emit(final VertexSelection selection) {
        if (!selection.equals(current)) {
            current = selection;
            List.copyOf(listeners).forEach(listener -> listener.accept(selection));
        }
    }

    /** The selection as it is. */
    VertexSelection current() {
        return current;
    }
}
