# homing-graph-view
An embeddable 3D explorer for dependency graphs, in JavaFX. Its features, the work on them and the quality checks on
this code live in [homing-graph-view-studio](https://github.com/captainssingapura-hue/homing-graph-view-studio);
nothing here depends on the studio.

To use it in a project of your own - from the ready-made viewer to your own shapes, edges and selections - see
[the guide](GUIDE.md).

## Modules

| Module | Holds | Depends on |
|---|---|---|
| `homing-graph-view-model` | the graph: `Vertex`, `Edge`, `Vertices`, `Edges` and `Graph`, vertices and edges only | Java 25, jOntology core |
| `homing-graph-view-layout` | what is read from a graph before it is drawn: `Closure`, `LayersByDepth` and its `Placement`, `Layers`, `LayerInBlock` with its `Ladder` and `Arrangement`, and `Focus` with `Highlight` and the default, `Reachability` | the model, jOntology core |
| `homing-graph-view-samples` | graphs to try the views on: `JdkModules`, the running JDK's modules, `RoyalFamily`, the British Royal Family in four generations, and `MadeUp`, layers of any size; not published | the model |
| `homing-graph-view-fx` | `GraphView`, an ordinary JavaFX node that draws a graph in 3D, with its camera, its figures, its edges in the style an `EdgeStyler` gives them, and emits a `VertexSelection` as its selection changes | the layout, JavaFX 25 |
| `homing-graph-view-viewer` | a ready-made window over `GraphView` for anyone to use: a `Registry` of `Exhibit`s, each a graph with its `Shapes`, its `Explanations` and its `EdgeStyler`, a layout choice and a details panel; `ViewerApplication` makes it an application | the view |
| `homing-graph-view-demo` | the viewer over the JDK's own modules and the British Royal Family; not published | the viewer, the samples |
| `homing-graph-view-bench` | a flat, 2D window over the layout, to look at it and polish it; not published | the layout, the samples, JavaFX 25 |
| `homing-graph-view-example-cats` | an example on its own, as anyone downstream would write one: cat breeds and the breeds they came from, in the viewer; not published | the viewer |

A vertex is its identity and an edge its link, from the vertex that depends to the vertex it depends on. A graph is the
product of its vertices and its edges, each an ordered vector, and is valid whenever it exists: every value refuses what
it can see is wrong when it is made. Each type says what it is in [jOntology](https://github.com/captainssingapura-hue/jOntology)'s
terms - `Scalar` or `Vector` - and the tests run jOntology's enforcer over all of them.

## Build

Needs JDK 25.

```bash
mvn install
```

Every module ships a sources jar beside its jar; the studio's quality checks read both.

For what a project of your own needs to depend on and how to set up its build, see
[setting up the build](GUIDE.md#setting-up-the-build) in the guide.

## The view

`new GraphView(graph)` is a JavaFX node like any other. It splits the graph into layers by depth, vertices on a cycle
sharing a layer, lays each layer out flat in its block (see [the flat layout](#the-flat-layout)) and stacks the layers
in the 3D space over a grid on the floor, as a `Staging` says:

- **The stance.** `Stance.Standing`, the default: each block upright like a monitor, its containers hanging on a
  board, the layers one behind another. `Stance.Lying`: each block flat, its containers sitting on a floor, the layers
  one above another like the storeys of a building; the camera starts square on to it.
- **The order.** `Order.BaseFar`, the default, puts level 0, the base's layer, at the far end of the stack: at the
  back standing, at the bottom lying. `Order.BaseNear` puts it at the near end: in front standing, on top lying.
- **The shapes.** `Shapes`, a FunctionalObject, says what each vertex is drawn as: a `Figure` for a vertex, a node
  facing the camera with the parts a selection colours, and the `Room` every figure fits in, which the layout sizes
  its slots by. The default, `ContainerShapes`, draws containers, `Container.Small`, `Half` or `Full` by length, as
  `Containers` says of each vertex (`Uniform` gives them all one size; full by default), each showing its vertex's ID
  on both long faces. Whatever its size, a figure takes one slot: how big a vertex is changes what is drawn, never the
  layout.

```java
new GraphView(graph, focus, new Staging().with(new Stance.Lying()).with(new Order.BaseNear()).with(shapes));
```

Each layer's platform, its board or its floor, is coloured as a map colours its states, no two neighbours alike. Each
block is shaped like a 16:9 screen, upright or laid flat, and centred on the stack's axis, so the stack looks even from
the side as from the front; each layer keeps far enough from the next, and lying, each row from the one in front, that
from the starting camera none hides another. A selection colours the figures it reaches and darkens the rest, and each
edge is drawn as its graph's styler says (see [edges](#edges)). A readout in the corner names the camera's mode,
what the keys do in it and what is selected.

The view emits an event each time its selection changes, by a click, by Esc, or by `select` and `clear`: a
`VertexSelection`, a plain value that is either `Some(vertex)` or `Nothing()`. A listener switches on the two, so
clearing what it shows is as easy as showing the next; setting the selection to what it already is tells no one.
Anything more, what to say of a vertex, say, is the listener's own:

```java
Subscription subscription = view.subscribe(event -> {
    switch (event) {
        case VertexSelection.Some(Vertex vertex) -> panel.show(vertex);
        case VertexSelection.Nothing() -> panel.clear();
    }
});
```

The camera has three modes, and Tab steps through them, keeping the view:

- **Orbit**, the start, framed on the whole graph: drag to turn, right-drag or Shift-drag to pan, and scroll to zoom
  toward the cursor.
- **Fly**, as a spectator does: W A S D move along the view, up or down included; Space rises and Ctrl sinks; Shift
  goes four times faster; the wheel changes speed; drag to look. W, A, S or D starts flying from an orbit.
- **Drone**: the same keys, but level along the heading, so only Space and Ctrl change its height, however it is
  tilted. The readout shows its altitude.

Speed eases toward what the keys ask, the same at any frame rate, and a free camera stays inside a box around the
graph, above the floor. 6 walks in: it stands on the floor in front of the graph, looking in. H glides back to the whole
graph, orbiting.

Click a slate to select it: the focus says what it lights, by default the vertex and everything it reaches. Esc clears
the selection, as does a click on empty space while orbiting.

`new GraphView(graph, focus)` asks another `Focus` what a selection means.

## Edges

What each edge looks like is said by an `EdgeStyler`, made downstream for one graph and handed to the view with it,
`new GraphView(graph, focus, staging, styler)`, which asks it of each of that graph's edges and of nothing else. A
styler can work out what it needs of its graph once, when it is made. It says only an `EdgeStyle`, a few attributes;
whatever an edge is, the view draws it:

- **Girth**, from -1, the thinnest, through 0 to 1, the thickest, on an exponential scale: each equal step makes an
  edge the same times thicker, three times from 0 to 1. What a girth measures is the view's, the same in every graph.
- **Colour**, red, green and blue, as `new Colour(0xd4af37)`; never see-through. It is the edge's colour while nothing
  is selected: a selection draws every edge in the colour of its state, as it does the figures.
- **Texture**, one of `Solid`, `Dashed`, `Dotted` and `Twin`, two thinner rods side by side.
- **Head**, `None` or an `Arrow` at the vertex depended on.

`SameStyle` gives every edge of any graph one style; `new SameStyle()`, a plain grey rod each, is the default. The
demo's styler for the royal family finds the family's marriages when it is made:

```java
record RoyalStyler(Edges marriages) implements EdgeStyler {

    RoyalStyler(final Graph family) {
        this(new Edges(family.edges().items().stream()
                .filter(edge -> family.edges().contains(new Edge(edge.to(), edge.from())))
                .toList()));
    }

    @Override
    public EdgeStyle style(final Edge edge) {
        if (marriages.contains(edge)) {
            return new EdgeStyle().with(new Texture.Twin()).with(new Colour(0xd4af37)).with(new Girth(0.5));
        }
        return new EdgeStyle().with(new Girth(-0.5));
    }
}
```

An edge runs from where its line leaves the figure that depends to where it enters the one depended on, so its head is
never lost inside a figure. A pair of edges each way between two vertices shares one line, split in the middle: each
edge draws the half by the vertex it depends on, in its own style and its own state. An edge between two vertices of
one layer runs straight across the layer, through any figure between them, so a graph does best to put such vertices
side by side in its order.

## The flat layout

`new LayerInBlock(ladder).of(layer)` lays one layer out in 2D, as a list fills an array. Which vertices make the
layer is settled before it is handed over: `new Layers().of(graph)` gives a graph's layers by depth, the base's first.

A `Ladder` gives the sizes a block comes in, as an ArrayList's capacities: from a single slot, each size holds at
least `growth` times the one before (1.5, as an ArrayList grows) and is as nearly the screen's shape as whole slots
allow. The layer's block is as wide as the smallest size that holds it, and as tall as the rows it fills. The layer
fills it in its own order, a row at a time from the top left, standing on the bottom, with a part-filled last row
centred.

An `Arrangement` is the block and a `Seat` for each vertex, a slot in lanes and rows, not coordinates: what a slot
measures is up to whatever draws it.

`new LayersByDepth(ladder).of(graph)` puts the two together for a whole graph: it splits the graph into layers and
lays each out in its own block, giving each vertex a `Placed` with its depth and its slot's lane and row. That one
`Placement` is what the 3D view draws from, whichever way it sets the blocks.

## Bench

After `mvn install`, a flat, 2D window over the layout, apart from the 3D view, to look at the layout and polish it:

```bash
mvn -pl homing-graph-view-bench javafx:run
```

It shows one layer at a time. The knobs along the top pick the graph (a JDK module, `ALL-SYSTEM`, or layer sizes
such as `2 8 30 90` for a made-up graph), which of its layers to show (the largest at first; Page Up and Page Down step
through them), the screen's shape and the ladder's growth.

## The viewer

`homing-graph-view-viewer` is a ready-made window over `GraphView`, to use as it is. Give it a `Registry` of
`Exhibit`s, each a graph with the functional objects it needs: its `Shapes`, what each vertex is drawn as, its
`Explanations`, what to say of a vertex selected, and its `EdgeStyler`, made for that graph, added with
`exhibit.with(styler)`. `new Exhibit(name, graph)` shows a graph plainly, in full containers explained by their names,
each edge a plain rod.

Down the left, a list of the exhibits and a choice of layout: horizontal, the layers one behind another, or vertical,
one above another. Picking another exhibit or layout draws it afresh in place of the last. Down the right, a details
panel shows the explanation of what is selected, kept up by listening to the view's selection events. Everything said
of a vertex comes from the exhibit's own explanations: the viewer only asks.

`ViewerApplication` makes it an application from one method:

```java
public final class Cats extends ViewerApplication {

    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    protected Registry registry(final List<String> args) {
        final Graph breeds = new Studbook().graph();
        return new Registry(List.of(new Exhibit("Cat breeds", breeds, new CatBadges(), new CatExplanations())
                        .with(new CatStyler(breeds)),
                new Exhibit("Cat breeds, plainly", breeds)));
    }
}
```

Its command-line arguments, in any order: `lying` starts in the vertical layout, `near` puts level 0 at the near end
of the stack, in front or on top, and any other word picks the exhibit to start on by part of its name.

## Demo

After `mvn install`:

```bash
mvn -pl homing-graph-view-demo javafx:run
```

The demo is the viewer over `java.se`, `java.desktop` and `jdk.jshell` with every module each requires, every module
of the JDK, and the British Royal Family. The JDK's modules are read from the running JDK, each a container sized by
its packages, small for up to 2, half for up to 9, full for more, and explained by its name. A member of the royal
family is explained by who they are, from a line each in the demo's `royal-family.txt`, then their parents, whom they
married and their children, read from the graph.

To start on another module, which joins the list, or on every module of the JDK:

```bash
mvn -pl homing-graph-view-demo javafx:run -Djavafx.args=java.desktop
```

```bash
mvn -pl homing-graph-view-demo javafx:run -Djavafx.args=ALL-SYSTEM
```

With the layers lying, like storeys, rather than standing, and with level 0 on top rather than at the bottom:

```bash
mvn -pl homing-graph-view-demo javafx:run -Djavafx.args="java.se lying"
```

```bash
mvn -pl homing-graph-view-demo javafx:run -Djavafx.args="java.se lying near"
```

The British Royal Family, from Queen Elizabeth II and Prince Philip to their great-grandchildren, its generations as
the layers and the eldest on top. A child points to each parent shown, and a marriage is a pair of edges, one each
way, so those who married in share their spouse's generation. It shows a shape provider of its own, `Portraits`: each
person is a gilt photo frame with their name on a plaque, holding a painted placeholder for their title, a crowned
king or queen, a prince in a coronet, a princess in a tiara, or a plain head and shoulders for anyone untitled. Its
styler, `RoyalStyler`, draws a marriage as a gold double line, as a family tree does, and a child's edge to a parent as
a thin grey rod:

```bash
mvn -pl homing-graph-view-demo javafx:run -Djavafx.args="royal lying near"
```

## Cat breeds

`homing-graph-view-example-cats` is an example on its own, written as anyone downstream would write one: it uses the
viewer and nothing else of this project's examples. Its studbook, `cat-breeds.txt`, has 35 breeds, a line each, with
its coat, the breeds it was bred from and who it is: as a graph, each breed points to the breeds it was bred from, so
the founders - natural breeds, mutations and two wild cats - are level 0. Each breed is a cat's-head badge in the
colour of its coat, and is explained by who it is, what it was bred from and what was bred from it. A cross with a wild
cat is dashed, in a wild cat's amber:

```bash
mvn -pl homing-graph-view-example-cats javafx:run -Djavafx.args="lying near"
```

## Licence

MIT.
