package hue.captains.singapura.homing.graphview.fx;

import hue.captains.singapura.homing.graphview.model.Edge;
import hue.captains.singapura.homing.graphview.model.Vertex;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** An edge's style: what a girth measures, what a colour is, the default, and every edge alike. */
class EdgeStyleTest {

    private static final double CLOSE = 1e-9;

    private final Vertex app = new Vertex("app");
    private final Vertex core = new Vertex("core");

    @Test
    void aGirthGoesFromMinusOneToOneEachEqualStepTheSameTimesThicker() {
        assertEquals(0.6, new Girth(-1).radius(), CLOSE);
        assertEquals(1.8, new Girth(0).radius(), CLOSE);
        assertEquals(5.4, new Girth(1).radius(), CLOSE);
        assertEquals(new Girth(0.5).radius() / new Girth(0).radius(),
                new Girth(-0.25).radius() / new Girth(-0.75).radius(), CLOSE);
        assertThrows(IllegalArgumentException.class, () -> new Girth(1.01));
        assertThrows(IllegalArgumentException.class, () -> new Girth(-1.5));
        assertThrows(IllegalArgumentException.class, () -> new Girth(Double.NaN));
    }

    @Test
    void aColourIsOpaqueRedGreenAndBlueAndCanBeWrittenAsAWebColourIs() {
        assertEquals(new Colour(212, 175, 55), new Colour(0xd4af37));
        assertEquals(Color.web("#d4af37"), new Colour(0xd4af37).color());
        assertThrows(IllegalArgumentException.class, () -> new Colour(256, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Colour(0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Colour(0x1000000));
        assertThrows(IllegalArgumentException.class, () -> new Colour(-1));
    }

    @Test
    void theDefaultIsThePlainGreyRodAndEveryEdgeAlikeIsGivenOneStyle() {
        final EdgeStyle plain = new EdgeStyle();
        assertEquals(new EdgeStyle(new Girth(0), new Colour(0x7b8698), new Texture.Solid(), new Head.None()), plain);
        assertEquals(new Palette().edge(new EdgeState.Idle()), plain.colour().color(), "the rods' grey");
        assertEquals(plain, new SameStyle().style(new Edge(app, core)));
        final EdgeStyle gold = plain.with(new Texture.Twin()).with(new Colour(0xd4af37)).with(new Girth(0.4))
                .with(new Head.Arrow());
        assertEquals(new EdgeStyle(new Girth(0.4), new Colour(0xd4af37), new Texture.Twin(), new Head.Arrow()), gold);
        assertEquals(gold, new SameStyle(gold).style(new Edge(core, app)));
        assertThrows(NullPointerException.class, () -> new SameStyle(null));
        assertThrows(NullPointerException.class, () -> plain.with((Texture) null));
    }
}
