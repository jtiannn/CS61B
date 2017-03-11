package grader;

import graph.Graph;
import graph.DirectedGraph;
import graph.UndirectedGraph;
import graph.ShortestPaths;
import graph.SimpleShortestPaths;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.nCopies;

import org.junit.Test;
import static org.junit.Assert.*;

/* You MAY add public @Test methods to this class.  You may also add
 * additional public classes containing "Test" in their name. These
 * may not be part of your graph package per se (that is, it must be
 * possible to remove them and still have your package work). */

/** Unit tests for the path algorithms in the graph package.
 *  @author P. N. Hilfinger
 */
public class PathTest {

    /*===== Miscellaneous Definitions =====*/

    /** Abbreviation for infinity. */
    static final double INF = Double.POSITIVE_INFINITY;

    /** Abbreviation for DirectedGraph. */
    private static class DG extends DirectedGraph {
    }

    /** Abbreviation for UndirectedGraph. */
    private static class UG extends UndirectedGraph {
    }

    /*===== Test Graphs =====*/

    /** Vertex count for test graph 1. */
    private static final int NV1 = 8;
    /** Edges for test graph 1. */
    private static final Object[][] E1 = {
        { 1, 2, 2.0 },
        { 1, 3, 5.0 },
        { 1, 4, 3.0 },
        { 1, 7, 7.0 },
        { 3, 2, 4.0 },
        { 2, 4, 5.0 },
        { 2, 5, 3.0 },
        { 5, 3, 2.0 },
        { 6, 3, 2.0 },
        { 7, 4, 3.0 },
        { 8, 4, 6.0 },
        { 4, 5, 4.0 },
        { 5, 8, 2.0 },
        { 5, 6, 1.0 },
        { 7, 8, 1.0 },
    };
    /** Shortest distances, directed. */
    private static final Double[] DG_W1 = {
        INF, 0.0, 2.0, 5.0, 3.0, 5.0, 6.0, 7.0, 7.0
    };
    /** Predecessors, directed. */
    private static final Integer[] DG_P1 = {
        0, 0, 1, 1, 1, 2, 5, 1, 5
    };
    /** Path from 1 to 8, directed. */
    private static final Integer[] DG_P1_8 = {
        1, 2, 5, 8
    };

    /** Shortest distances, undirected. */
    private static final Double[] UG_W1 = {
        INF, 0.0, 2.0, 5.0, 3.0, 5.0, 6.0, 6.0, 7.0
    };
    /** Predecessors, undirected. */
    private static final Integer[] UG_P1 = {
        0, 0, 1, 1, 1, 2, 5, 4, 5
    };

    /** Map Data (from Russell & Norvig, 3rd ed).  [See Note 1 at end.] */
    private static final int NV2 = 20;
    private static final Object[][] E2 = {
        { 13, 20, 71.0 },
        { 20, 1, 75.0 },
        { 13, 16, 151.0 },
        { 1, 16, 140.0 },
        { 1, 17, 118.0 },
        { 17, 10, 111.0 },
        { 10, 11, 70.0 },
        { 11, 4, 75.0 },
        { 4, 3, 120.0 },
        { 16, 15, 80.0 },
        { 15, 3, 146.0 },
        { 16, 6, 99.0 },
        { 15, 14, 97.0 },
        { 3, 14, 138.0 },
        { 6, 2, 211.0 },
        { 14, 2, 101.0 },
        { 2, 7, 90.0 },
        { 2, 18, 85.0 },
        { 18, 19, 142.0 },
        { 19, 9, 92.0 },
        { 9, 12, 87.0 },
        { 18, 8, 98.0 },
        { 8, 5, 161.0 },
    };
    /** Direct distances to 2. */
    private static final double[] H2 = {
        INF,
        366.0, 0.0, 160.0, 242.0, 161.0, 176.0, 77.0, 151.0,
        226.0, 244.0, 241.0, 234.0, 380.0,
        100.0, 193.0, 253.0, 329.0, 80.0, 199.0, 374.0,
    };
    private static final Integer[] P2_2 = {
        1, 16, 15, 14, 2
    };

    /*===== Utilities =====*/

    /** Set up _G with NV vertices and the edges given in EDGES, whose
     *  entries are (V1 index, V2 index, edge weight). */
    private void fillWeightedGraph(Graph G, int nv, Object[][] edges) {
        _G = G;
        _W = new ArrayList<>();
        _W.add(null);
        for (int i = 1; i <= nv; i += 1) {
            int v = _G.add();
            _W.add(new ArrayList<Double>(nCopies(nv + 1,
                                                 Double.POSITIVE_INFINITY)));
            assertEquals("Bad vertex number returned by add", i, v);
        }
        for (Object[] e : edges) {
            _G.add((Integer) e[0], (Integer) e[1]);
            _W.get((Integer) e[0]).set((Integer) e[1], (Double) e[2]);
            if (!G.isDirected()) {
                _W.get((Integer) e[1]).set((Integer) e[0], (Double) e[2]);
            }
        }
    }

    /** Return the list of weights from SP, indexed by vertex number
     *  (missing vertices are INF). The maximum vertex number is MV. */
    List<Double> getWeights(ShortestPaths sp, int mv) {
        ArrayList<Double> R = new ArrayList<>(nCopies(mv + 1, INF));
        for (int i = 1; i <= mv; i += 1) {
            R.set(i, sp.getWeight(i));
        }
        return R;
    }

    /** Return the list of weights from SP, indexed by vertex number
     *  (missing vertices are INF). The maximum vertex number is MV. */
    List<Integer> getPreds(ShortestPaths sp, int mv) {
        ArrayList<Integer> R = new ArrayList<>(nCopies(mv + 1, 0));
        for (int i = 1; i <= mv; i += 1) {
            R.set(i, sp.getPredecessor(i));
        }
        return R;
    }

    /*===== Tests =====*/

    class SP1 extends SimpleShortestPaths {

        SP1(int source) {
            super(PathTest.this._G, source);
        }

        @Override
        public double getWeight(int u, int v) {
            return _W.get(u).get(v);
        }
    }

    /** Test all-points shortest paths with directed edges. */
    @Test(timeout = 1000)
    public void directedShortestPaths() {
        fillWeightedGraph(new DG(), NV1, E1);
        ShortestPaths sp = new SP1(1);
        sp.setPaths();
        assertEquals("wrong weights", asList(DG_W1), getWeights(sp, NV1));
        assertEquals("wrong predecessors", asList(DG_P1), getPreds(sp, NV1));
        assertEquals("bad path", asList(DG_P1_8), sp.pathTo(8));
    }

    /** Test all-points shortest paths with undirected edges. */
    @Test(timeout = 1000)
    public void undirectedShortestPaths() {
        fillWeightedGraph(new UG(), NV1, E1);
        ShortestPaths sp = new SP1(1);
        sp.setPaths();
        assertEquals("wrong weights", asList(UG_W1), getWeights(sp, NV1));
        assertEquals("wrong predecessors", asList(UG_P1), getPreds(sp, NV1));
        assertEquals("bad path", asList(DG_P1_8), sp.pathTo(8));
    }

    class SP2 extends SimpleShortestPaths {

        SP2(int source, int dest, double[] h) {
            super(PathTest.this._G, source, dest);
            _h = h;
        }

        @Override
        public double getWeight(int u, int v) {
            return _W.get(u).get(v);
        }

        @Override
        protected double estimatedDistance(int v) {
            return _h[v];
        }

        private double[] _h;

    }

    /** Test A* search with directed edges.  Check that certain vertices are
     *  not visited. */
    @Test(timeout = 1000)
    public void directedAStar() {
        fillWeightedGraph(new DG(), NV2, E2);
        ShortestPaths sp = new SP2(1, 2, H2);
        sp.setPaths();
        assertEquals("wrong path to Bucharest", asList(P2_2), sp.pathTo(2));
        assertEquals("looked too far", 0, sp.getPredecessor(18));
        assertEquals("heuristic didn't work", 0, sp.getPredecessor(10));
    }


    /** Test A* search with undirected edges.  Check that certain vertices are
     *  not visited. */
    @Test(timeout = 1000)
    public void undirectedAStar() {
        fillWeightedGraph(new UG(), NV2, E2);
        ShortestPaths sp = new SP2(1, 2, H2);
        sp.setPaths();
        assertEquals("wrong path to Bucharest", asList(P2_2), sp.pathTo(2));
        assertEquals("looked too far", 0, sp.getPredecessor(18));
        assertEquals("heuristic didn't work", 0, sp.getPredecessor(10));
    }


    /** The test graph. */
    private Graph _G;
    private ArrayList<ArrayList<Double>> _W;


    /* NOTE 1.  From S. Russell and P. Norvig, _Artificial Intelligence: A
                Modern Approach_, 3rd ed, Prentice Hall, 2010, Figures 3.2,
                3.22.  Original data from 3.22:

                1 Arad        366
                2 Bucharest   0
                3 Craiova     160
                4 Drobeta     242
                5 Eforie      161
                6 Fagaras     176
                7 Giurgiu     77
                8 Hirsova     151
                9 Iasi        226
                10 Lugoj      244
                11 Mehadia    241
                12 Neamt      234
                13 Oradea     380
                14 Pitesti    100
                15 Rimnicu Vilcea
                              193
                16 Sibiu      253
                17 Timisoara  329
                18 Urziceni   80
                19 Vaslui     199
                20 Zerind     374
    */

}
