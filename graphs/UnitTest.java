package grader;

import ucb.junit.textui;

/* You MAY add public @Test methods to this class.  You may also add
 * additional public classes containing "Test" in their name. These
 * may not be part of your graph package per se (that is, it must be
 * possible to remove them and still have your package work). */

/** Unit tests for the graph package.
 *  @author P. N. Hilfinger
 */
public class UnitTest {

    /** Run all JUnit tests in the graph package. */
    public static void main(String... ignored) {
        System.exit(textui.runClasses(grader.GraphTest.class,
                                      grader.LabeledGraphTest.class,
                                      grader.PathTest.class));
    }

}
