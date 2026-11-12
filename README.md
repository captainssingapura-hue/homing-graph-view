# homing-graph-view
An embeddable 3D explorer for dependency graphs, in JavaFX. Its features, the work on them and the quality checks on
this code live in [homing-graph-view-studio](https://github.com/captainssingapura-hue/homing-graph-view-studio);
nothing here depends on the studio.

## Modules

| Module | Holds | Depends on |
|---|---|---|
| `homing-graph-view-model` | the graph: `Vertex`, `Edge`, `Vertices`, `Edges` and `Graph`, vertices and edges only | Java 25, jOntology core |
| `homing-graph-view-layout` | what is read from a graph before it is drawn: `Closure`, `LayersByDepth` and its `Placement`, and `Focus` with `Highlight` and the default, `Reachability` | the model, jOntology core |
| `homing-graph-view-fx` | `GraphView`, an ordinary JavaFX node that draws a graph in 3D, with its camera, slates and rods | the layout, JavaFX 25 |
| `homing-graph-view-demo` | a window over `GraphView` showing the JDK's own modules; not published | the view |

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

`new GraphView(graph)` is a JavaFX node like any other. It lays the graph out in layers by depth, the vertices that
depend on nothing furthest back and vertices on a cycle sharing a layer, over a grid on the floor. It draws each vertex
as a slate showing its ID on both faces and each edge as a rod. A readout in the corner names the camera's mode and what
the keys do in it.

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

## Licence

MIT.
