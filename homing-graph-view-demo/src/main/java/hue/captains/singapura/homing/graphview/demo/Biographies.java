package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Who each person in the royal family is, in a line, read from {@code royal-family.txt} beside this class: a name, then
 * {@code " = "}, then who they are. Lines starting with {@code #} are notes.
 */
record Biographies() implements StatelessFunctionalObject {

    private static final String BETWEEN = " = ";

    /**
     * Who the person is, or nothing for a name with no line.
     *
     * @param name the person's name, as in the royal family's graph
     */
    String of(final String name) {
        try (InputStream stream = Objects.requireNonNull(Biographies.class.getResourceAsStream("royal-family.txt"),
                "The demo is missing royal-family.txt");
             BufferedReader lines = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return lines.lines().filter(line -> !line.startsWith("#") && line.startsWith(name + BETWEEN))
                    .map(line -> line.substring(name.length() + BETWEEN.length()).strip())
                    .findFirst().orElse("");
        } catch (final IOException unread) {
            throw new UncheckedIOException("Cannot read royal-family.txt", unread);
        }
    }
}
