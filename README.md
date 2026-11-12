# homing-graph-view
An embeddable 3D explorer for dependency graphs, in JavaFX. Its features, the work on them and the quality checks on
this code live in [homing-graph-view-studio](https://github.com/captainssingapura-hue/homing-graph-view-studio);
nothing here depends on the studio.

## Modules

| Module | Holds | Depends on |
|---|---|---|
| `homing-graph-view-model` | the graph: `Vertex`, `Edge`, `Vertices`, `Edges` and `Graph`, vertices and edges only | Java 25, jOntology core |
| `homing-graph-view-layout` | what is read from a graph before it is drawn: `Closure`, `LayersByDepth` and its `Placement`, `Layers`, `LayerInBlock` with its `Ladder` and `Arrangement`, and `Focus` with `Highlight` and the default, `Reachability` | the model, jOntology core |
| `homing-graph-view-samples` | graphs to try the views on: `JdkModules`, the running JDK's modules, and `MadeUp`, layers of any size; not published | the model |
| `homing-graph-view-fx` | `GraphView`, an ordinary JavaFX node that draws a graph in 3D, with its camera, slates and rods | the layout, JavaFX 25 |
| `homing-graph-view-demo` | a window over `GraphView` showing the JDK's own modules; not published | the view, the samples |
| `homing-graph-view-bench` | a flat, 2D window over the layout, to look at it and polish it; not published | the layout, the samples, JavaFX 25 |

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

## The view

`new GraphView(graph)` is a JavaFX node like any other. It splits the graph into layers by depth, vertices on a cycle
sharing a layer, lays each layer out flat in its block (see [the flat layout](#the-flat-layout)) and stacks the layers
in the 3D space over a grid on the floor, as a `Staging` says:

- **The stance.** `Stance.Standing`, the default: each block upright like a monitor, its containers hanging on a
  board, the layers one behind another. `Stance.Lying`: each block flat, its containers sitting on a floor, the layers
  one above another like the storeys of a building; the camera starts square on to it.
- **The order.** `Order.BaseFar`, the default, puts level 0, the base's layer, at the far end of the stack: at the
  back standing, at the bottom lying. `Order.BaseNear` puts it at the near end: in front standing, on top lying.
- **The containers.** Each vertex is a container, `Container.Small`, `Half` or `Full` by length, as `Containers` says
  of it (`Uniform` gives them all one size; full by default). Whatever its size, a container takes one slot: how big a
  vertex is changes what is drawn, never the layout.

```java
new GraphView(graph, focus, new Staging().with(new Stance.Lying()).with(new Order.BaseNear()).with(containers));
```

Each layer's platform, its board or its floor, is coloured as a map colours its states, no two neighbours alike. Each
block is shaped like a 16:9 screen, upright or laid flat, and each layer keeps far enough from the next that, from the
starting camera, none hides another. Each container shows its vertex's ID on both its long faces, smaller on a short
container, and each edge is a rod. A readout in the corner names the camera's mode, what the keys do in it and what is
selected.

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

## Demo

After `mvn install`:

```bash
mvn -pl homing-graph-view-demo javafx:run
```

It shows `java.se` and every module it requires, read from the running JDK, each module a container sized by its
packages: small for up to 2, half for up to 9, full for more. To start from another module, or from every module of
the JDK:

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

## Licence

MIT.
