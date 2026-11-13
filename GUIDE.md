# Using homing-graph-view in your own project

This guide is for a project that wants to show its own graph in 3D: modules and what they require, a build's
dependencies, a family tree, anything of vertices and edges. It goes from adding the library to your build, through
showing a graph in the ready-made viewer in a few lines, to embedding the view in an application of your own and giving
it your own shapes, edges, selections and explanations.

Every example here is taken from one small project, built against the library as a downstream project would be:
a graph of four modules, `app`, `web`, `store` and `core`. The library's own `homing-graph-view-example-cats` module
is a second, larger example, written the same way (see [a complete example](#a-complete-example-cat-breeds)).

- [What to depend on](#what-to-depend-on)
- [Setting up the build](#setting-up-the-build)
- [Your graph](#your-graph)
- [The quickest way: the ready-made viewer](#the-quickest-way-the-ready-made-viewer)
- [Embedding GraphView in your own window](#embedding-graphview-in-your-own-window)
- [What each vertex is drawn as: Shapes](#what-each-vertex-is-drawn-as-shapes)
- [How each edge is drawn: EdgeStyler](#how-each-edge-is-drawn-edgestyler)
- [What a selection means: Focus](#what-a-selection-means-focus)
- [What the details panel says: Explanations](#what-the-details-panel-says-explanations)
- [The layout on its own, without 3D](#the-layout-on-its-own-without-3d)
- [Keeping to jOntology](#keeping-to-jontology)
- [For the people using your application](#for-the-people-using-your-application)
- [Limits and tips](#limits-and-tips)
- [A complete example: cat breeds](#a-complete-example-cat-breeds)

## What to depend on

| You want | Depend on | Brings in |
|---|---|---|
| a window that shows graphs, with a list of them, a layout choice and a details panel | `homing-graph-view-viewer` | the view, the layout and the model |
| the 3D view as a JavaFX node in a window of your own | `homing-graph-view-fx` | the layout and the model |
| layers and flat layouts, to draw your own way, with no JavaFX | `homing-graph-view-layout` | the model |
| only the graph values | `homing-graph-view-model` | jOntology core |

Every module's group is `io.github.captainssingapura-hue.homing.graphview`. The samples, the demo, the bench and the
cat breeds example are not meant to be depended on.

## Setting up the build

You need JDK 25 and Maven. The library is at version `0.1.0-SNAPSHOT`, so install it into your local Maven repository
first, from a clone of this repository:

```bash
mvn install
```

Then, in your project's `pom.xml`, the viewer (or `homing-graph-view-fx` for the view alone), jOntology's core, whose
markers your own types will implement, and JavaFX:

```xml
<properties>
    <maven.compiler.release>25</maven.compiler.release>
    <homing.graphview.version>0.1.0-SNAPSHOT</homing.graphview.version>
    <jontology.version>0.1.0</jontology.version>
    <javafx.version>25.0.1</javafx.version>
</properties>

<dependencies>
    <dependency>
        <groupId>io.github.captainssingapura-hue.homing.graphview</groupId>
        <artifactId>homing-graph-view-viewer</artifactId>
        <version>${homing.graphview.version}</version>
    </dependency>
    <dependency>
        <groupId>io.github.captainssingapura-hue.lang.ontology</groupId>
        <artifactId>core</artifactId>
        <version>${jontology.version}</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>${javafx.version}</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-maven-plugin</artifactId>
            <version>0.0.8</version>
            <configuration>
                <mainClass>com.example.deps.MyViewer</mainClass>
            </configuration>
        </plugin>
    </plugins>
</build>
```

`mvn javafx:run` then starts your application, with JavaFX where it needs to be. The library's jars have no module
descriptors; use them from the class path.

To run your application some other way, mind that Java refuses to start a main class that extends JavaFX's
`Application` while JavaFX is on the class path, with "JavaFX runtime components are missing". Either put JavaFX on
the module path (`--module-path <the JavaFX jars> --add-modules javafx.controls`), or start from a plain main class
that launches the application:

```java
public final class Launcher implements StatelessFunctionalObject {

    public static void main(final String[] args) {
        Application.launch(MyViewer.class, args);
    }
}
```

## Your graph

A graph is its vertices and its edges, and nothing else:

- a `Vertex` is an ID, never blank, unique in its graph;
- an `Edge` goes `from` the vertex that depends `to` the vertex it depends on, the way declared dependencies point,
  never from a vertex to itself;
- `Vertices` and `Edges` are ordered, each item once;
- a `Graph` refuses an edge whose ends are not among its vertices.

Each refuses what is wrong when it is made, so a graph that exists is a valid one. Anything else you know about a
vertex - its size, its kind, its description - stays in your own data, for your shapes, styler and explanations to
look up by the vertex's ID.

```java
public record Modules() implements StatelessFunctionalObject {

    public Graph graph() {
        final Map<String, List<String>> requires = new LinkedHashMap<>();
        requires.put("app", List.of("web", "store"));
        requires.put("web", List.of("core"));
        requires.put("store", List.of("core"));
        requires.put("core", List.of());

        final Map<String, Vertex> vertices = new LinkedHashMap<>();
        requires.keySet().forEach(name -> vertices.put(name, new Vertex(name)));
        final List<Edge> edges = new ArrayList<>();
        requires.forEach((name, needs) -> needs.forEach(
                need -> edges.add(new Edge(vertices.get(name), vertices.get(need)))));
        return new Graph(new Vertices(List.copyOf(vertices.values())), new Edges(edges));
    }
}
```

How the graph is laid out follows from its edges and its order:

- **Layers.** A vertex's layer, its depth, is the longest path from it to a vertex that depends on nothing. Those are
  level 0, the base: `core` here. `web` and `store` are level 1 and `app` level 2.
- **Cycles.** Vertices on a cycle share a layer. Two edges each way make two vertices peers, as the demo's royal family
  makes a married couple.
- **Order.** Within its layer, each vertex takes the next slot in the graph's order, a row at a time from the top left.
  Put vertices that belong together next to each other in your `Vertices`.

## The quickest way: the ready-made viewer

The viewer is a window with your graphs in a list down the left, a choice of layout under it, the graph picked in the
middle and a details panel down the right. Extend `ViewerApplication` and give it a `Registry` of `Exhibit`s, each a
graph with a name:

```java
public final class MyViewer extends ViewerApplication {

    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    protected Registry registry(final List<String> args) {
        final Graph modules = new Modules().graph();
        return new Registry(List.of(
                new Exhibit("Modules", modules, new Bubbles(), new Counts()).with(new ScopeStyler(modules)),
                new Exhibit("Modules, plainly", modules)));
    }
}
```

An exhibit is a graph with the functional objects it needs:

| Part | Says | Default |
|---|---|---|
| `Shapes` | what each vertex is drawn as | `new ContainerShapes(new Uniform(new Container.Full()))`, a full container each |
| `Explanations` | what the details panel says of a vertex selected | `new Names()`, its ID |
| `EdgeStyler` | how each edge is drawn, made for this exhibit's graph | `new SameStyle()`, a plain grey rod each |

`new Exhibit(name, graph)` takes all three defaults, `new Exhibit(name, graph, shapes, explanations)` the default
edges, and `exhibit.with(styler)` gives an exhibit its styler. A registry keeps its exhibits' order, needs at least
one and refuses two of one name.

The application's command-line arguments are read in any order: `lying` starts in the vertical layout, `near` puts
level 0 at the near end of the stack, and any other word starts on the first exhibit whose name holds it, ignoring
case. With the plugin:

```bash
mvn javafx:run -Djavafx.args="plainly lying near"
```

`registry(args)` is given the same arguments, for an application that reads more from them; the demo, for one, adds a
JDK module asked for that it does not list.

To put the viewer in a window of your own rather than its own application, make one and place its node:

```java
Viewer viewer = new Viewer(registry, new Staging(), 0, stage::setTitle);
stage.setScene(new Scene(viewer.node(), 1400, 820));
```

Its arguments are the registry, how to stage the exhibits to begin with (the stance and the order; each exhibit
brings its own shapes), which exhibit to start on, and where to say what is shown.

## Embedding GraphView in your own window

`GraphView` is an ordinary JavaFX node, a `Region`, that draws one graph in 3D. Put it anywhere in a scene:

```java
public final class Embedded extends Application implements Mutable {

    private Subscription listening;

    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) {
        final Graph graph = new Modules().graph();
        final GraphView view = new GraphView(graph, new Dependents(),
                new Staging().with(new Stance.Lying()).with(new Order.BaseNear()).with(new Bubbles()),
                new ScopeStyler(graph));
        final Label status = new Label("Nothing selected");
        listening = view.subscribe(event -> {
            switch (event) {
                case VertexSelection.Some(Vertex vertex) -> status.setText("Selected " + vertex.id());
                case VertexSelection.Nothing() -> status.setText("Nothing selected");
            }
        });
        final BorderPane root = new BorderPane(view);
        root.setBottom(status);
        stage.setScene(new Scene(root, 1200, 800));
        stage.show();
        view.requestFocus();
        view.select(new Vertex("core"));
    }

    @Override
    public void stop() {
        listening.unsubscribe();
    }
}
```

Its constructors each add one thing to the last:

| Constructor | Adds |
|---|---|
| `new GraphView(graph)` | the graph; everything else by default |
| `new GraphView(graph, focus)` | what a selection means; `new Reachability()` by default |
| `new GraphView(graph, focus, staging)` | how the layers are set and stacked, and what each vertex is drawn as |
| `new GraphView(graph, focus, staging, styler)` | how each edge is drawn: a styler made for this graph |

A `Staging` is three choices, each with a default, each changed by `with`:

- **The stance.** `Stance.Standing`, the default, sets each layer upright like a monitor, its figures on a board,
  the layers one behind another. `Stance.Lying` lays each layer flat like a floor, the layers one above another like
  storeys.
- **The order.** `Order.BaseFar`, the default, puts level 0 at the far end of the stack: at the back standing, at the
  bottom lying. `Order.BaseNear` puts it at the near end: in front standing, on top lying.
- **The shapes.** What each vertex is drawn as; see [Shapes](#what-each-vertex-is-drawn-as-shapes).

The view's own methods:

| Method | Does |
|---|---|
| `subscribe(listener)` | tells the listener of each change of the selection, and returns the `Subscription` that ends it |
| `select(vertex)` | selects a vertex of the graph, as a click on it does |
| `clear()` | clears the selection, as Esc does |
| `selected()` | what is selected now: a `VertexSelection` |
| `selection()` | what the selection lights, as its focus said, empty when nothing is selected |
| `home()` | glides the camera back to the whole graph |

The selection event, `VertexSelection`, is either `Some(vertex)` or `Nothing()`. Switch on the two, so that clearing
what you show is as easy as showing the next. A listener hears only changes, from a click, from Esc, or from your own
`select` and `clear`: setting the selection to what it already is tells no one. Nothing is selected to begin with.

A few things to know:

- **Build and use it on the JavaFX application thread**, as any node.
- **Give it focus** with `requestFocus()` for its keys to work before it is first clicked; a press on it takes focus.
- **It is drawn once.** A view's graph, staging and styler are fixed. To change any of them, make a new view and put
  it in place of the old one, which is what the viewer does when another exhibit or layout is picked.
- **It has a readout** in its top-left corner, naming the camera's mode, what the keys do and what is selected.
- **It asks for 960 by 640** when nothing sizes it, and fills whatever room it is given.

## What each vertex is drawn as: Shapes

`Shapes` is a functional object with two methods:

```java
public interface Shapes extends FunctionalObject {
    Room room();              // the room every figure fits in
    Figure of(Vertex vertex); // the figure for a vertex: a new node each time
}
```

**The room** is one size for every figure of the graph: its width, height and depth, in scene units. The layout gives
every vertex a slot a little larger than the room, whatever is drawn in it, and frames the camera by it. A vertex's
importance can show in what is drawn, but never in the space it takes.

**A figure** is your node and the parts of it a selection colours:

```java
new Figure(node, List.of(theParts, aSelectionColours));
```

- **Centre it on its own origin**, inside the room, its front facing the camera, toward negative z. The view moves it
  into its slot.
- **Make a new node each time**: a node lives in one place in a scene.
- **The tinted parts** are `Shape3D`s. With nothing selected they keep their own colours; a selection paints them in
  the colour of the vertex's state: blue for the vertex selected, green for the rest it lights, amber for anything it
  marks, and a dark grey for everything else.
- **Every other `Shape3D`** keeps its colour, as a cat badge's eyes do.
- **Flat parts** - labels, images, canvases - are greyed and darkened when their figure is dimmed.
- **Nothing see-through.** Never set opacity or a transparent colour: a 3D scene draws see-through parts out of order.
  Dimming is done with colour, for the same reason.
- **Words** are best a `Label` of fixed size, placed a hair in front of the face it is on, so the two do not flicker.

```java
/** Each vertex a ball, with its name across the front. */
public record Bubbles() implements Shapes {

    private static final double SIZE = 80;

    @Override
    public Room room() {
        return new Room(SIZE, SIZE, SIZE);
    }

    @Override
    public Figure of(final Vertex vertex) {
        final Sphere ball = new Sphere(SIZE / 2);
        ball.setMaterial(new PhongMaterial(Color.web("#3d6b8f")));
        final Label name = new Label(vertex.id());
        name.setTextFill(Color.WHITE);
        name.setAlignment(Pos.CENTER);
        name.setMinSize(SIZE, 20);
        name.setPrefSize(SIZE, 20);
        name.setMaxSize(SIZE, 20);
        name.setMouseTransparent(true);
        name.setTranslateX(-SIZE / 2);
        name.setTranslateY(-10);
        name.setTranslateZ(-SIZE / 2 - 1);
        return new Figure(new Group(ball, name), List.of(ball));
    }
}
```

A click anywhere on a figure selects its vertex: the view finds it from the node it wraps your figure in.

### The default: containers

`ContainerShapes` draws each vertex as a shipping container with its ID on both long faces, in smaller type when the
ID is long. A `Containers` says which size each vertex comes in: `Container.Small`, a quarter of a full one's length,
`Container.Half` or `Container.Full`. All three take the same slot. `Uniform` gives every vertex one size:

```java
new ContainerShapes(new Uniform(new Container.Full())) // the default
new ContainerShapes(new Sized(graph))                  // a size of your choosing for each vertex
```

```java
/** A module's container by how much requires it: small for nothing, half for one or two, full for more. */
public record Sized(Graph graph) implements Containers {

    @Override
    public Container of(final Vertex vertex) {
        final int users = graph.edges().to(vertex).size();
        if (users == 0) {
            return new Container.Small();
        }
        return users <= 2 ? new Container.Half() : new Container.Full();
    }
}
```

## How each edge is drawn: EdgeStyler

An `EdgeStyler` says how each edge of one graph is drawn. Make it for that graph and hand it over with it: to
`new GraphView(graph, focus, staging, styler)`, or to an exhibit with `with(styler)`. The view asks it of each of
that graph's edges, once, and of nothing else. It answers with an `EdgeStyle`, a few attributes; the view draws the
edge from them. There is no drawing edges your own way.

| Attribute | Values |
|---|---|
| `Girth` | from -1, the thinnest, through 0, the default, to 1, the thickest; each step makes an edge the same times thicker, three times from 0 to 1 |
| `Colour` | red, green and blue, as `new Colour(0xd4af37)` or `new Colour(212, 175, 55)`; never see-through |
| `Texture` | `Solid`, `Dashed`, `Dotted`, or `Twin`, two thinner rods side by side |
| `Head` | `None`, or an `Arrow` at the vertex depended on |

`new EdgeStyle()` is a plain grey rod of girth 0 with no head; `with` changes one attribute at a time.
`new SameStyle(style)` gives every edge of any graph one style; `new SameStyle()`, a plain rod each, is the default.

A styler can work out what it needs of its graph once, when it is made, and keep it, as long as what it keeps is a
value (see [jOntology](#keeping-to-jontology)):

```java
/** Edges into core thick and amber, with arrowheads; every other edge dashed and thin. */
public record ScopeStyler(Edges intoCore) implements EdgeStyler {

    /** The styler for a graph: finds its edges into core once, as it is made. */
    public ScopeStyler(final Graph graph) {
        this(graph.edges().to(new Vertex("core")));
    }

    @Override
    public EdgeStyle style(final Edge edge) {
        if (intoCore.contains(edge)) {
            return new EdgeStyle().with(new Girth(0.6)).with(new Colour(0xe0a030)).with(new Head.Arrow());
        }
        return new EdgeStyle().with(new Texture.Dashed()).with(new Girth(-0.6));
    }
}
```

What the view does with each edge:

- **Ends.** An edge runs from where its line leaves the figure that depends to where it enters the one depended on,
  so a head is never hidden inside a figure.
- **Pairs.** Two edges each way between two vertices share one line, split in the middle. Each edge draws the half by
  the vertex it depends on, in its own style and its own state; two halves styled alike read as one link.
- **Colour.** An edge's colour is its own while nothing is selected. A selection draws it blue between two vertices it
  lights, amber to a vertex it marks, and dark otherwise, as it does the figures.
- **Straight lines.** An edge is straight. One between two vertices of the same layer runs across the layer, through
  any figure between them: put such vertices next to each other in your graph's order.

## What a selection means: Focus

A `Focus` decides what selecting a vertex shows, as a `Highlight`:

| Part | Drawn |
|---|---|
| `root` | the vertex selected, in blue |
| `members` | the vertices it lights, the root among them, in green |
| `marked` | vertices to point out but not light, in amber |
| `lit` | edges between members, in blue |
| `markedEdges` | edges from a member to a marked vertex, in amber |

Everything else is dimmed. The default, `Reachability`, lights the vertex and everything it depends on, directly or
not, and marks nothing: right for modules and builds. Another focus can light anything. This one lights what depends
on the vertex instead:

```java
/** Selecting a vertex lights what depends on it, directly or not, rather than what it depends on. */
public record Dependents() implements Focus, StatelessFunctionalObject {

    @Override
    public Highlight of(final Graph graph, final Vertex root) {
        final Set<Vertex> users = new LinkedHashSet<>(List.of(root));
        final Deque<Vertex> next = new ArrayDeque<>(users);
        while (!next.isEmpty()) {
            for (final Edge edge : graph.edges().to(next.pop()).items()) {
                if (users.add(edge.from())) {
                    next.push(edge.from());
                }
            }
        }
        final Vertices members = new Vertices(graph.vertices().items().stream().filter(users::contains).toList());
        final Edges lit = new Edges(graph.edges().items().stream()
                .filter(edge -> users.contains(edge.from()) && users.contains(edge.to()))
                .toList());
        return new Highlight(root, members, new Vertices(List.of()), lit, new Edges(List.of()));
    }
}
```

A highlight refuses a root that is not a member, and a vertex both a member and marked. `graph.edges().from(vertex)`
and `graph.edges().to(vertex)` find a vertex's edges each way in constant time.

The viewer always uses `Reachability`; give `GraphView` another focus to change it.

## What the details panel says: Explanations

In the viewer, selecting a vertex asks the exhibit's `Explanations` what to say of it: an `Explanation`, a heading and
a body, which may be empty and may run to several lines. It is given the graph too, for anything to say of the
vertex's edges:

```java
/** A module explained by what it requires and what requires it. */
public record Counts() implements Explanations {

    @Override
    public Explanation of(final Vertex vertex, final Graph graph) {
        return new Explanation(vertex.id(), "Requires " + graph.edges().from(vertex).size()
                + ", required by " + graph.edges().to(vertex).size() + ".");
    }
}
```

`Names`, the default, gives the vertex's ID and nothing more. For what is not in the graph, look it up by the vertex's
ID: the demo's royal family reads a line about each person from a resource file, and the cat breeds read theirs from a
studbook. Outside the viewer, do the same from your own selection listener.

## The layout on its own, without 3D

`homing-graph-view-layout` is the part that decides where everything goes, with no JavaFX, for drawing a graph your
own way. Nothing in it is geometry: it gives slots, and what a slot measures is up to you.

- `new Layers().of(graph)` splits a graph into its layers, a `Vertices` each, level 0 first.
- A `Ladder(slot, screen, growth)` gives the sizes a layer's block comes in, as an `ArrayList`'s capacities: from a
  single slot, each size holds at least `growth` times the one before, as nearly the screen's shape as whole slots
  allow. `slot` and `screen` are each a width over a height.
- `new LayerInBlock(ladder).of(layer)` lays one layer out: an `Arrangement`, its `Block` and a `Seat` for each vertex,
  a `Slot` of a lane across, centred on 0, and a row up, from 0 at the bottom.
- `new LayersByDepth(ladder).of(graph)` does both for a whole graph: a `Placement`, a `Placed` for each vertex with its
  depth, lane and row.

```java
final Graph graph = new Modules().graph();
final Ladder ladder = new Ladder(1, 16.0 / 9, 1.5);
final List<Vertices> layers = new Layers().of(graph);
for (int depth = 0; depth < layers.size(); depth++) {
    final Arrangement arrangement = new LayerInBlock(ladder).of(layers.get(depth));
    System.out.println("layer " + depth + ": " + arrangement.block().columns() + " by " + arrangement.rows());
    for (final Seat seat : arrangement.items()) {
        System.out.println("  " + seat.vertex().id() + " lane " + seat.slot().lane() + " row " + seat.slot().row());
    }
}
for (final Placed placed : new LayersByDepth(ladder).of(graph).items()) {
    System.out.println(placed.vertex().id() + ": depth " + placed.depth() + ", lane " + placed.lane()
            + ", row " + placed.row());
}
```

```text
layer 0: 1 by 1
  core lane 0.0 row 0
layer 1: 2 by 1
  web lane -0.5 row 0
  store lane 0.5 row 0
layer 2: 1 by 1
  app lane 0.0 row 0
app: depth 2, lane 0.0, row 0
web: depth 1, lane -0.5, row 0
store: depth 1, lane 0.5, row 0
core: depth 0, lane 0.0, row 0
```

`Closure` gives a vertex and everything it reaches, the members `Reachability` lights.

## Keeping to jOntology

Every type in the library says what it is in [jOntology](https://github.com/captainssingapura-hue/jOntology)'s terms,
and the interfaces you implement carry those terms: `Shapes`, `Containers`, `EdgeStyler`, `Focus` and `Explanations`
are all `FunctionalObject`s. Nothing checks your types while your application runs, but they are expected to keep the
contract, and jOntology's enforcer can check them in a test.

| Marker | For | The enforcer requires |
|---|---|---|
| `Scalar` | a value | final fields, each of a value type; no setters; no static methods but `main` |
| `Vector` | an ordered collection that is a value | the same, except that it may keep its items in a collection |
| `FunctionalObject` | behaviour with fixed settings | the same as a `Scalar` |
| `StatelessFunctionalObject` | behaviour with no settings at all | no instance fields, and no static methods but `main` |
| `Mutable` | anything that changes: applications, nodes | only that it is not marked as one of the others too |

A value type is a primitive, `String` and the JDK's other value types such as `Optional` and `Instant`, an enum, or a
type marked `Scalar`, `Vector` or `FunctionalObject` that keeps its own contract. A class with no marker fails. In
practice:

- **Write records.** A `record` of values is a `Scalar` or `FunctionalObject` that needs nothing more.
- **Keep what a styler works out as a value.** `ScopeStyler` keeps its edges as `Edges`, not a `List` or a `Set`; a
  `Map` of your own data can become a `Vector` of your own, or be read when asked, as the cat breeds' studbook is.
- **No caches.** A functional object cannot remember what it was asked; work it out when the object is made instead.
- **No static methods but `main`.** Constants are fine. Put helpers on an object, even a stateless record.
- **Sealed interfaces of records rather than enums**, for your own kinds: an enum's own generated static methods fail
  the check when the enum is checked. The library's `Container`, `Stance`, `Texture` and `VertexSelection` are made so.
- **Mark the rest.** Your `Application` subclass is `Mutable`; a class with only `main` is a
  `StatelessFunctionalObject`.

To check, add jOntology's `enforcer` and `enforcing-utils` at test scope, and JUnit, and run the enforcer over every
type of your package:

```java
class ContractTest {

    @Test
    void everyTypeKeepsTheContractItDeclares() throws IOException {
        final CodeSource main = MyViewer.class.getProtectionDomain().getCodeSource();
        final List<Class<?>> types = new PackageScanner().scan(MyViewer.class.getPackageName()).stream()
                .filter(type -> main.equals(type.getProtectionDomain().getCodeSource()))
                .toList();
        final OntologyEnforcer enforcer = new OntologyEnforcer();
        final List<String> violations = types.stream()
                .flatMap(type -> enforcer.enforce(type).stream())
                .map(ContractViolation::message)
                .toList();
        assertEquals(List.of(), violations);
    }
}
```

## For the people using your application

What the view does with the mouse and keys, for your own help page:

| | Does |
|---|---|
| click a figure | selects its vertex |
| Esc, or a click on empty space while orbiting | clears the selection |
| Tab | steps through the camera's modes: orbit, fly, drone |
| H or Home | glides back to the whole graph, orbiting |
| 6 | walks in: stands on the floor in front of the graph, looking in |

- **Orbit**, the start, framed on the whole graph: drag to turn, right-drag or Shift-drag to pan, and the wheel zooms
  toward the cursor.
- **Fly**: W A S D move along the view, up or down included; Space rises and Ctrl sinks; Shift goes four times
  faster; the wheel changes speed; drag to look. W, A, S or D starts flying from an orbit.
- **Drone**: the same keys, but level along the heading, so only Space and Ctrl change its height.

A free camera stays inside a box around the graph, above the floor. The readout in the corner always says the mode
and what the keys do in it.

## Limits and tips

- **Everything happens when a view is made.** Shapes, the styler and the layout are asked once, for every vertex and
  edge; keep them quick for large graphs. The demo draws every module of the JDK; much larger graphs are untested.
- **One room per view.** Every figure gets the same room, so a figure larger than it crowds its neighbours; show size
  inside the room, as containers do.
- **Edges take no clicks**, and there is no edge selection event.
- **No see-through anything**, figures or edges.
- **Ask only about your own graph.** A styler is asked only about its own graph's edges; make one for each graph.
  `select` takes only a vertex of the view's graph.
- **Order matters.** Your graph's order is the order each layer fills its block. Put vertices with edges between them
  in one layer side by side.
- **Snapshot versions change.** At `0.1.0-SNAPSHOT`, the API may still change between versions.

## A complete example: cat breeds

`homing-graph-view-example-cats`, in this repository, is written exactly as a downstream project would write it: it
depends on the viewer and nothing else of this project's modules.

| File | Does |
|---|---|
| `cat-breeds.txt` | the data: a line for each breed, with its coat, the breeds it was bred from and a description |
| `Studbook` | reads the file into a graph, each breed pointing to the breeds it was bred from, and looks a breed up by name |
| `Breed`, `Coat` | a breed's own data, values outside the graph; `Coat` a sealed interface of records |
| `CatBadges` | its `Shapes`: a cat's head in the colour of the breed's coat, with its name on a plate |
| `CatExplanations` | its `Explanations`: who the breed is, what it was bred from and what was bred from it |
| `CatStyler` | its `EdgeStyler`: made for the graph, it finds the wild cats once, and dashes a cross with one |
| `Cats` | its `ViewerApplication`: the breeds twice, as badges and plainly |
| `CatsTest` | the graph's layers, the explanations, the styler, the registry, and every type's contract |

```bash
mvn -pl homing-graph-view-example-cats javafx:run -Djavafx.args="lying near"
```

The demo, `homing-graph-view-demo`, is a second: the JDK's own modules in containers sized by their packages, and the
British Royal Family in portraits, married couples joined in gold.
