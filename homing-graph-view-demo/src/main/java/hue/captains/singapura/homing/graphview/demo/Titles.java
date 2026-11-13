package hue.captains.singapura.homing.graphview.demo;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/** The title a name begins with: King Charles III a king, Princess Anne a princess, Peter Phillips none. */
record Titles() implements StatelessFunctionalObject {

    /**
     * The title.
     *
     * @param name a person's name, title first if they have one
     */
    Title of(final String name) {
        final String first = name.strip().split(" ", 2)[0];
        return switch (first) {
            case "King" -> new Title.King();
            case "Queen" -> new Title.Queen();
            case "Prince" -> new Title.Prince();
            case "Princess" -> new Title.Princess();
            default -> new Title.Untitled();
        };
    }
}
