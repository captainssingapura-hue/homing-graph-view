package hue.captains.singapura.homing.graphview.viewer;

import hue.captains.singapura.tao.ontology.Vector;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/** The exhibits a viewer shows, in the order its list gives them, no two of one name. */
public final class Registry implements Vector<Exhibit> {

    private final List<Exhibit> items;
    private final int hash;

    /**
     * The exhibits given, in their order.
     *
     * @param items the exhibits, at least one, each of a name of its own
     */
    public Registry(final List<Exhibit> items) {
        this.items = List.copyOf(items);
        if (this.items.isEmpty()) {
            throw new IllegalArgumentException("A registry needs an exhibit to show");
        }
        final Set<String> names = new HashSet<>();
        for (final Exhibit exhibit : this.items) {
            if (!names.add(exhibit.name())) {
                throw new IllegalArgumentException("Two exhibits are called " + exhibit.name());
            }
        }
        this.hash = this.items.hashCode();
    }

    /** The exhibits, in order; the list cannot be changed. */
    public List<Exhibit> items() {
        return items;
    }

    /** How many exhibits there are. */
    public int size() {
        return items.size();
    }

    /**
     * Where the first exhibit whose name holds the words given is in the list, ignoring case; empty if none does.
     *
     * @param words part of a name, such as royal for the British Royal Family
     */
    public Optional<Integer> find(final String words) {
        final String sought = words.toLowerCase(Locale.ROOT);
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).name().toLowerCase(Locale.ROOT).contains(sought)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    /** Equal to another registry with the same exhibits in the same order. */
    @Override
    public boolean equals(final Object other) {
        return other instanceof Registry that && hash == that.hash && items.equals(that.items);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return "Registry" + items.stream().map(Exhibit::name).toList();
    }
}
