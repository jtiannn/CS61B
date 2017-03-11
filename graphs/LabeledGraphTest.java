package grader;

import graph.DirectedGraph;
import graph.UndirectedGraph;
import graph.LabeledGraph;

import org.junit.Test;
import static org.junit.Assert.*;

/* You MAY add public @Test methods to this class.  You may also add
 * additional public classes containing "Test" in their name. These
 * may not be part of your graph package per se (that is, it must be
 * possible to remove them and still have your package work). */

/** Unit tests for LabeledGraph.
 *  @author P. N. Hilfinger
 */
public class LabeledGraphTest {

    /*===== Test Graphs =====*/

    /** Vertex count for test graph 1. */
    private static final int NV1 = 10;
    /** Edges for test graph 1. */
    private static final Integer[][] E1 = {
        { 2, 5 }, { 2, 3 },
        { 2, 6 }, { 3, 7 }, { 3, 8 }, { 8, 1 }, { 8, 9 },
        { 1, 2 }, { 1, 3 }, { 1, 4 },
        { 8, 10 }, { 10, 7 } };

    /** Edges for test graph 2 (graph 1 with 2 removed). */
    private static final Integer[][] E2 = {
        { 3, 7 }, { 3, 8 }, { 8, 1 }, { 8, 9 },
        { 1, 3 }, { 1, 4 },
        { 8, 10 }, { 10, 7 } };

    /*===== Miscellaneous Definitions =====*/

    /** Abbreviation for DirectedGraph. */
    private static class LDG extends LabeledGraph<String, String> {
        LDG() {
            super(new DirectedGraph());
        }
    }

    /** Abbreviation for UndirectedGraph. */
    private static class LUG extends LabeledGraph<String, String> {
        LUG() {
            super(new UndirectedGraph());
        }
    }

    /*===== Utilities =====*/

    /** Set up _G with NV vertices and the edges given in EDGES, whose
     *  entries are (V1 index, V2 index). */
    private void fillGraph(LabeledGraph<String, String> G,
                           int nv, Integer[][] edges) {
        _G = G;
        for (int i = 1; i <= nv; i += 1) {
            int v = _G.add("V" + i);
            assertEquals("Bad vertex number returned by add", i, v);
        }
        for (Integer[] e : edges) {
            if (_G.isDirected() || e[0] <= e[1]) {
                _G.add(e[0], e[1], "E" + e[0] + "-" + e[1]);
            } else {
                _G.add(e[0], e[1], "E" + e[1] + "-" + e[0]);
            }
        }
    }

    /*===== Tests =====*/

    /** Check for right labels on vertices. */
    @Test(timeout = 1000)
    public void vertexLabelsDirected() {
        fillGraph(new LDG(), NV1, E1);
        for (int v : _G.vertices()) {
            assertEquals("wrong vertex label for " + v,
                         "V" + v, _G.getLabel(v));
        }
    }

    /** Check for right labels on vertices. */
    @Test(timeout = 1000)
    public void vertexLabelsUndirected() {
        fillGraph(new LUG(), NV1, E1);
        for (int v : _G.vertices()) {
            assertEquals("wrong vertex label for " + v,
                         "V" + v, _G.getLabel(v));
        }
    }

    /** Check for right labels on edges. */
    @Test(timeout = 1000)
    public void edgeLabelsDirected() {
        fillGraph(new LDG(), NV1, E1);
        for (int[] e : _G.edges()) {
            assertEquals(String.format("wrong edge label for (%d, %d)",
                                       e[0], e[1]),
                         String.format("E%d-%d", e[0], e[1]),
                         _G.getLabel(e[0], e[1]));
        }
    }

    /** Check for right labels on edges. */
    @Test(timeout = 1000)
    public void edgeLabelsUndirected() {
        fillGraph(new LUG(), NV1, E1);
        for (int[] e : _G.edges()) {
            assertEquals(String.format("wrong edge label for (%d, %d)",
                                       e[0], e[1]),
                         String.format("E%d-%d", Math.min(e[0], e[1]),
                                       Math.max(e[0], e[1])),
                         _G.getLabel(e[0], e[1]));
        }
    }

    /** The test graph. */
    private LabeledGraph<String, String> _G;
}
