package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.homing.graphview.fx.ContainerShapes;
import hue.captains.singapura.homing.graphview.samples.JdkModules;
import hue.captains.singapura.homing.graphview.samples.RoyalFamily;
import hue.captains.singapura.homing.graphview.viewer.Arguments;
import hue.captains.singapura.homing.graphview.viewer.Exhibit;
import hue.captains.singapura.homing.graphview.viewer.Names;
import hue.captains.singapura.homing.graphview.viewer.Registry;
import hue.captains.singapura.homing.graphview.viewer.ViewerApplication;
import java.util.ArrayList;
import java.util.List;

/**
 * The demo: the viewer over the JDK's own modules, each a container sized by its packages and explained by its name,
 * and over the British Royal Family, each person a portrait explained by who they are and their family. An argument
 * naming a JDK module not on the list, as java.xml, adds it at the top and starts on it.
 */
public final class Demo extends ViewerApplication {

    /** Starts the demo. */
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    protected Registry registry(final List<String> args) {
        final List<Exhibit> exhibits = new ArrayList<>(List.of(modules("java.se"), modules("java.desktop"),
                modules("jdk.jshell"), modules("ALL-SYSTEM"), royals()));
        new Arguments().asked(args)
                .filter(asked -> new Registry(exhibits).find(asked).isEmpty())
                .ifPresent(asked -> {
                    try {
                        exhibits.addFirst(modules(asked));
                    } catch (final IllegalArgumentException noSuchModule) {
                        System.err.println(noSuchModule.getMessage());
                    }
                });
        return new Registry(exhibits);
    }

    /** A JDK module and every module it requires, or every module for ALL-SYSTEM. */
    private Exhibit modules(final String root) {
        final String name = "ALL-SYSTEM".equals(root) ? "JDK: every module (ALL-SYSTEM)" : "JDK: " + root;
        return new Exhibit(name, new JdkModules().of(root), new ContainerShapes(new PackageCounts()), new Names());
    }

    /** The British Royal Family, in portraits. */
    private Exhibit royals() {
        return new Exhibit("British Royal Family", new RoyalFamily().of(), new Portraits(), new RoyalExplanations());
    }
}
