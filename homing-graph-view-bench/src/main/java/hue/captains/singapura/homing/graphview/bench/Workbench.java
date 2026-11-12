package hue.captains.singapura.homing.graphview.bench;

import hue.captains.singapura.homing.graphview.layout.Arrangement;
import hue.captains.singapura.homing.graphview.layout.Ladder;
import hue.captains.singapura.homing.graphview.layout.LayerInBlock;
import hue.captains.singapura.homing.graphview.layout.Layers;
import hue.captains.singapura.homing.graphview.model.Vertices;
import hue.captains.singapura.tao.ontology.Mutable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import java.util.List;
import java.util.stream.IntStream;

/**
 * The bench: the knobs along the top - the graph, which of its layers, the screen's shape and how the ladder grows -
 * with a readout under them, and the layer drawn below. The graph is split into layers here, before a layer is handed
 * to the layout. Page Up and Page Down step through the layers.
 */
final class Workbench implements Mutable {

    private static final double SPACING = 10;
    private static final double PADDING = 10;
    private static final double TEXT_SIZE = 13;

    private final BorderPane root = new BorderPane();
    private final ComboBox<String> graphs = new ComboBox<>();
    private final ComboBox<String> layerPicker = new ComboBox<>();
    private final ComboBox<String> screens = new ComboBox<>();
    private final ComboBox<String> growths = new ComboBox<>();
    private final Label readout = new Label();
    private final Label hint = new Label("Pick a layer, or step through them with Page Up and Page Down. Scroll to "
            + "zoom, drag to pan, F or a double click to fit. Type layer sizes, as 2 8 30 90, for a made-up graph.");
    private final Sheet sheet = new Sheet();
    private final Colours colours = new Colours();
    private List<Vertices> layers = List.of();
    private boolean filling;

    /**
     * The bench, starting on a graph.
     *
     * @param first a JDK module's name, ALL-SYSTEM, or layer sizes
     */
    Workbench(final String first) {
        graphs.setEditable(true);
        graphs.getItems().setAll("java.se", "java.desktop", "jdk.jshell", "ALL-SYSTEM", "1 3 9 27 81",
                "2 8 30 90", "4 16 60 200");
        graphs.setValue(first);
        screens.getItems().setAll("16:9", "21:9", "3:2", "4:3", "1:1");
        screens.setValue("16:9");
        growths.getItems().setAll("1.25", "1.5", "2", "3");
        growths.setValue("1.5");
        graphs.setOnAction(event -> load());
        List.of(layerPicker, screens, growths).forEach(box -> box.setOnAction(event -> lay()));
        final HBox knobs = new HBox(SPACING, label("Graph"), graphs, label("Layer"), layerPicker, label("Screen"),
                screens, label("Growth"), growths);
        knobs.setAlignment(Pos.CENTER_LEFT);
        List.of(readout, hint).forEach(this::style);
        final VBox top = new VBox(SPACING / 2, knobs, readout, hint);
        top.setPadding(new Insets(PADDING));
        top.setBackground(Background.fill(colours.block()));
        root.setTop(top);
        root.setCenter(sheet.node());
        root.addEventFilter(KeyEvent.KEY_PRESSED, this::step);
        load();
    }

    /** The bench's node. */
    BorderPane node() {
        return root;
    }

    /** Reads the graph named and splits it into layers, offering them with the largest picked; or says why not. */
    private void load() {
        try {
            layers = new Layers().of(new Sources().of(graphs.getValue()));
        } catch (final IllegalArgumentException refused) {
            readout.setText(refused.getMessage());
            return;
        }
        filling = true;
        layerPicker.getItems().setAll(IntStream.range(0, layers.size())
                .mapToObj(depth -> new Readout().name(depth, layers.get(depth))).toList());
        layerPicker.getSelectionModel().select(IntStream.range(0, layers.size()).boxed()
                .max((one, other) -> Integer.compare(layers.get(one).size(), layers.get(other).size())).orElse(-1));
        filling = false;
        lay();
    }

    /** Lays the layer picked out with the knobs as they are, and draws it; or says why it cannot. */
    private void lay() {
        final int depth = layerPicker.getSelectionModel().getSelectedIndex();
        if (filling || depth < 0) {
            return;
        }
        try {
            final Ladder ladder = new Ladder(Drawing.SLOT_WIDTH / Drawing.SLOT_HEIGHT, ratio(screens.getValue()),
                    Double.parseDouble(growths.getValue()));
            final Arrangement arrangement = new LayerInBlock(ladder).of(layers.get(depth));
            sheet.show(new Drawing(arrangement));
            readout.setText(new Readout().of(layers, depth, arrangement, ladder));
        } catch (final IllegalArgumentException refused) {
            readout.setText(refused.getMessage());
        }
    }

    /** Page Up picks the layer above, Page Down the layer below. */
    private void step(final KeyEvent event) {
        final int by = event.getCode() == KeyCode.PAGE_UP ? 1 : event.getCode() == KeyCode.PAGE_DOWN ? -1 : 0;
        final int depth = layerPicker.getSelectionModel().getSelectedIndex() + by;
        if (by != 0 && depth >= 0 && depth < layers.size()) {
            layerPicker.getSelectionModel().select(depth);
            event.consume();
        }
    }

    /** A screen's width over its height, from its name, as 16:9. */
    private double ratio(final String screen) {
        final String[] sides = screen.split(":");
        return Double.parseDouble(sides[0]) / Double.parseDouble(sides[1]);
    }

    private Label label(final String text) {
        final Label label = new Label(text);
        style(label);
        return label;
    }

    private void style(final Label label) {
        label.setFont(Font.font(TEXT_SIZE));
        label.setTextFill(colours.caption());
    }
}
