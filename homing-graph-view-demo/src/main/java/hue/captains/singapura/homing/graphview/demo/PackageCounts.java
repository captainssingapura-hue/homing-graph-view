package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.homing.graphview.fx.Container;
import hue.captains.singapura.homing.graphview.fx.Containers;
import hue.captains.singapura.homing.graphview.model.Vertex;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;

/**
 * A JDK module's container, by how many packages it has: a small one for up to {@value #SMALL}, a half one for up to
 * {@value #HALF}, and a full one for more. The JDK's modules split about evenly between the three.
 */
record PackageCounts() implements Containers {

    private static final int SMALL = 2;
    private static final int HALF = 9;

    @Override
    public Container of(final Vertex vertex) {
        final int packages = ModuleFinder.ofSystem().find(vertex.id()).map(ModuleReference::descriptor)
                .map(descriptor -> descriptor.packages().size()).orElse(0);
        return packages <= SMALL ? new Container.Small() : packages <= HALF ? new Container.Half()
                : new Container.Full();
    }
}
