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
sharing a layer, lays each layer out flat in its block (see [the flat layout](#the-flat-layout)) and sets the blocks in
the 3D space in a `Stance`, over a grid on the floor:

- **Standing**, the default: each block upright like a monitor, rows going up, the layers one behind another with the
  root's in front and the base's at the back.
- **Lying**, `new GraphView(graph, focus, new Stance.Lying())`: each block flat like a floor, rows going back, the
  layers one above another with the base's at the bottom.

Either way, each block is shaped to look about 16:9 from the starting camera, and each layer keeps far enough from the
next that, looking straight in from the starting pitch, none hides another. Each vertex is a slate showing its ID on
both faces, and each edge a rod. A readout in the corner names the camera's mode and what the keys do in it.

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

It shows `java.se` and every module it requires, read from the running JDK. To start from another module, or from
every module of the JDK:

```bash
mvn -pl homing-graph-view-demo javafx:run -Djavafx.args=java.desktop
```

```bash
mvn -pl homing-graph-view-demo javafx:run -Djavafx.args=ALL-SYSTEM
```

With the layers lying, like floors, rather than standing:

```bash
mvn -pl homing-graph-view-demo javafx:run -Djavafx.args="java.se lying"
```

## Licence

MIT.
