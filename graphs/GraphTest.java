package grader;

import graph.Graph;
import graph.DirectedGraph;
import graph.UndirectedGraph;
import graph.DepthFirstTraversal;
import graph.BreadthFirstTraversal;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.awt.Point;

import static java.util.Arrays.asList;
import static java.util.Collections.max;

import org.junit.Test;
import static org.junit.Assert.*;

/* You MAY add public @Test methods to this class.  You may also add
 * additional public classes containing "Test" in their name. These
 * may not be part of your graph package per se (that is, it must be
 * possible to remove them and still have your package work). */

/** Unit tests for the Graph class.
 *  @author P. N. Hilfinger
 */
public class GraphTest {

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

    private static final int NV3 = 11;
    private static final Integer[][] E3 = {
        { 1, 2 }, { 2, 5 }, { 5, 9 }, { 1, 3 }, { 1, 4 },
        { 3, 6 }, { 6, 10 }, { 4, 11 }, { 3, 7 }, { 4, 8 },
        { 8, 11 }, { 7, 11 } };

    /** As for E1, but with some self-edges thrown in. */
    private static final int NV4 = 10;
    private static final Integer[][] E4 = {
        { 2, 5 }, { 2, 3 },
        { 2, 6 }, { 3, 7 }, { 3, 8 }, { 8, 1 }, { 8, 9 },
        { 1, 1 }, { 8, 8 }, { 2, 2 },
        { 1, 2 }, { 1, 3 }, { 1, 4 },
        { 8, 10 }, { 10, 7 } };

    /** Graph E4 with vertex 2 removed. */
    private static final int NV5 = 10;
    private static final Integer[][] E5 = {
        { 3, 7 }, { 3, 8 }, { 8, 1 }, { 8, 9 },
        { 1, 1 }, { 8, 8 },
        { 1, 3 }, { 1, 4 },
        { 8, 10 }, { 10, 7 } };

    /** Returns a 2-D list of N empty 1-D lists of TYPE */
    static <T> ArrayList<ArrayList<T>> empty2DList(int N, Class<T> type) {
        ArrayList<ArrayList<T>> L = new ArrayList<>();
        for (int i = 0; i < N; i += 1) {
            L.add(new ArrayList<T>());
        }
        return L;
    }

    /*===== Factories =====*/

    Graph dg() {
        return new DirectedGraph();
    }

    Graph ug() {
        return new UndirectedGraph();
    }

    /*===== Utilities =====*/

    /** Returns a map of vertex numbers, v,  in the range [0 .. MAXVERTEX-1] to
     *  lists of successor vertices of v in EDGES. */
    static ArrayList<ArrayList<Integer>> successorEdges(Integer[][] edges,
                                                        int maxVertex) {
        ArrayList<ArrayList<Integer>> S =
            empty2DList(maxVertex + 1, Integer.class);

        for (Integer[] e : edges) {
            S.get(e[0]).add(e[1]);
        }
        return S;
    }


    /** Returns a map of vertex numbers, v,  in the range [0 .. MAXVERTEX-1] to
     *  lists of predecessor vertices of v in EDGES. */
    static ArrayList<ArrayList<Integer>> predecessorEdges(Integer[][] edges,
                                                          int maxVertex) {
        ArrayList<ArrayList<Integer>> S =
            empty2DList(maxVertex + 1, Integer.class);

        for (Integer[] e : edges) {
            S.get(e[1]).add(e[0]);
        }
        return S;
    }

    /** Returns a map of vertex numbers, v, in the range [0 .. MAXVERTEX-1] to
     *  lists of neighboring vertices of v in EDGES. */
    static ArrayList<ArrayList<Integer>> neighborEdges(Integer[][] edges,
                                                       int maxVertex) {
        ArrayList<ArrayList<Integer>> S =
            empty2DList(maxVertex + 1, Integer.class);

        for (Integer[] e : edges) {
            S.get(e[0]).add(e[1]);
            if (e[0] != e[1]) {
                S.get(e[1]).add(e[0]);
            }
        }
        return S;
    }

    /** Set up _G with NV vertices and the edges given in EDGES, whose
     *  entries are (V1 index, V2 index). */
    private void fillGraph(Graph G, int nv, Integer[][] edges) {
        _G = G;
        for (int i = 1; i <= nv; i += 1) {
            int v = _G.add();
            assertEquals("Bad vertex number returned by add", i, v);
        }
        for (Integer[] e : edges) {
            _G.add(e[0], e[1]);
        }
    }

    /** Returns a list of items produced by iterator IT. */
    static <T> ArrayList<T> toList(Iterator<T> it) {

        ArrayList<T> r = new ArrayList<>();
        while (it.hasNext()) {
            r.add(it.next());
        }
        return r;
    }

    /** Return true iff EDGES contains an edge from V0 to V1. */
    private boolean contains(boolean directed, int v0, int v1,
                             Integer[][] edges) {
        for (Integer[] e : edges) {
            if ((e[0] == v0 && e[1] == v1)
                || (!directed && e[0] == v1 && e[1] == v0)) {
                return true;
            }
        }
        return false;
    }

    /** Check containment of all pairs (u, v) in _G against EDGES. */
    private void checkContains(Integer[][] edges) {
        for (int u : _G.vertices()) {
            for (int v : _G.vertices()) {
                assertEquals(String.format(".contains(%d, %d) wrong", u, v),
                             contains(_G.isDirected(), u, v, edges),
                             _G.contains(u, v));
            }
        }
    }

    /** Check that _G, a directed graph, has vertex set VERTICES and edges
     *  as described by EDGES.  Use PREFIX as prefix of error messages. */
    private void checkDirectedGraph(String prefix,
                                    List<Integer> vertices,
                                    Integer[][] edges) {
        int maxv = max(vertices);
        assertEquals(prefix + "Wrong vertex count (directed graph)",
                     vertices.size(), _G.vertexSize());
        assertEquals(prefix + "Wrong vertices (directed graph)",
                     vertices, toList(_G.vertices()));
        assertEquals(prefix + "Wrong directed edge count",
                     edges.length, _G.edgeSize());

        checkContains(edges);

        ArrayList<ArrayList<Integer>>
            successors = successorEdges(edges, maxv),
            predecessors = predecessorEdges(edges, maxv);

        assertEquals(prefix + "Wrong directed edges",
                     edgeSet(successors, true), edgeSet(_G.edges(), true));

        for (int v = 1; v <= maxv; v += 1) {
            assertEquals(prefix + "Wrong outgoing directed edges for " + v,
                         successors.get(v), toList(_G.successors(v)));
            assertEquals(prefix + "Wrong outgoing directed edge count for " + v,
                         successors.get(v).size(), _G.outDegree(v));
            assertEquals(prefix + "Wrong incoming directed edges for " + v,
                         predecessors.get(v), toList(_G.predecessors(v)));
            assertEquals(prefix + "Wrong incoming directed edge count for " + v,
                         predecessors.get(v).size(), _G.inDegree(v));
            assertEquals(prefix + "Wrong undirected neighbors for " + v,
                         successors.get(v), toList(_G.neighbors(v)));
            assertEquals(prefix + "Wrong degree for " + v,
                         successors.get(v).size(), _G.degree(v));
        }
    }

    /** Check that G, a directed graph, has vertex set VERTICES and edges
     *  as described by EDGES.  Use PREFIX as prefix of error messages. */
    void checkDirectedGraph(Graph G, String prefix,
                            List<Integer> vertices, Integer[][] edges) {
        _G = G;
        checkDirectedGraph(prefix, vertices, edges);
    }

    /** Check that _G, an undirected graph, has vertex set VERTICES and edges
     *  as described by EDGES.  Use PREFIX as prefix of error messages. */
    private void checkUndirectedGraph(String prefix,
                                      List<Integer> vertices,
                                      Integer[][] edges) {
        int maxv = max(vertices);
        assertEquals(prefix + "Wrong vertex count (undirected)",
                     vertices.size(), _G.vertexSize());
        assertEquals(prefix + "Wrong vertices (undirected)",
                     vertices, toList(_G.vertices()));
        assertEquals(prefix + "Wrong undirected edge count",
                     edges.length, _G.edgeSize());

        checkContains(edges);

        ArrayList<ArrayList<Integer>> neighbors = neighborEdges(edges, maxv);

        assertEquals(prefix + "Wrong directed edges",
                     edgeSet(neighbors, false), edgeSet(_G.edges(), false));

        for (int v = 1; v <= maxv; v += 1) {
            assertEquals(prefix + "wrong outgoing undirected edges for " + v,
                         neighbors.get(v), toList(_G.successors(v)));
            assertEquals(prefix
                         + "wrong outgoing undirected edge count for " + v,
                         neighbors.get(v).size(), _G.outDegree(v));
            assertEquals(prefix + "wrong incoming undirected edges for " + v,
                         neighbors.get(v), toList(_G.predecessors(v)));
            assertEquals(prefix
                         + "wrong incoming undirected edge count for " + v,
                         neighbors.get(v).size(), _G.inDegree(v));
            assertEquals(prefix + "wrong undirected neighbors for " + v,
                         neighbors.get(v), toList(_G.neighbors(v)));
            assertEquals(prefix + "wrong degree for " + v,
                         neighbors.get(v).size(), _G.degree(v));
        }
    }

    /** Check that G, an undirected graph, has vertex set VERTICES and edges
     *  as described by EDGES.  Use PREFIX as prefix of error messages. */
    void checkUndirectedGraph(Graph G, String prefix,
                              List<Integer> vertices,
                              Integer[][] edges) {
        _G = G;
        checkUndirectedGraph(prefix, vertices, edges);
    }

    /** Return map of reachable vertices from V0 to breadth-first
     *  distances from V0 in graph with successor map SUCCS. */
    static Map<Integer, Integer>
        levelMap(final ArrayList<ArrayList<Integer>> succs, int v0) {
        final HashMap<Integer, Integer> distMap = new HashMap<>();
        class BF {
            void bf(int u, int d, int maxDepth) {
                if (!distMap.containsKey(u)) {
                    distMap.put(u, d);
                }
                if (d < maxDepth) {
                    for (int v : succs.get(u)) {
                        bf(v, d + 1, maxDepth);
                    }
                }
            }
        }
        BF b = new BF();
        int len;
        len = -1;
        for (int d = 0; len != distMap.size(); d += 1) {
            len = distMap.size();
            b.bf(v0, 0, d);
        }
        return distMap;
    }

    /** Returns true iff the SEQ are all in the domain of
     *  LEVELMAP and are ordered by their mapped values. */
    private boolean levelOrdered(Map<Integer, Integer> levelMap,
                                 List<Integer> seq) {
        int last;
        last = -1;
        for (int s : seq) {
            int lev = levelMap.get(s);
            if (!levelMap.containsKey(s) || last > lev || last < lev - 1) {
                return false;
            }
            last = lev;
        }
        return true;
    }

    /** Returns String.format(FORMAT, ARGS). */
    private static String fmt(String format, Object... args) {
        return String.format(format, args);
    }

    /** Prints graph represented by SUCCS on the standard error. */
    private static void printGraph(ArrayList<ArrayList<Integer>> succs) {
        for (int v = 1; v < succs.size(); v += 1) {
            ArrayList<Integer> next = succs.get(v);
            if (next.size() > 0) {
                System.err.printf("%d ->", v);
                for (int w : next) {
                    System.err.printf(" %d", w);
                }
                System.err.println();
            }
        }
    }

    /** Returns a set of edges given a map, SUCCESSORS, of vertex
     *  numbers to lists of successor vertices.   Treats the edges as directed
     *  iff DIRECTED.   If undirected, edges are in canonical order (u, v) with
     *  u <= v. */
    static Set<Point>
        edgeSet(ArrayList<ArrayList<Integer>> edges, boolean directed) {
        HashSet<Point> result = new HashSet<>();
        for (int u = 1; u < edges.size(); u += 1) {
            for (int v : edges.get(u)) {
                if (directed || u <= v) {
                    result.add(new Edge(u, v));
                } else {
                    result.add(new Edge(v, u));
                }
            }
        }
        return result;
    }

    /** Returns the set of edges returned by IT, which are directed iff
     *  DIRECTED. */
    static Set<Point> edgeSet(Iterator<int[]> it, boolean directed) {
        HashSet<Point> result = new HashSet<>();
        while (it.hasNext()) {
            int[] e = it.next();
            assertEquals("bad edge array", 2, e.length);
            if (directed || e[0] <= e[1]) {
                result.add(new Edge(e[0], e[1]));
            } else {
                result.add(new Edge(e[1], e[0]));
            }
        }
        return result;
    }


    static void report(String title, ArrayList<ArrayList<Integer>> graph,
                       String listTitle, List<Integer> list) {
        System.err.printf("%n%s details.%n", title);
        System.err.printf("Graph:%n");
        printGraph(graph);
        if (list != null) {
            System.err.printf("%s:%n", listTitle);
            for (int v : list) {
                if (v > 0) {
                    System.err.printf(" %d", v);
                } else {
                    System.err.printf(" <%d>", -v);
                }
            }
            System.err.println();
        }
        System.err.printf("--------%n");
    }

    /*===== Tests =====*/

    /** Check properties of empty graph. */
    @Test(timeout = 1000)
    public void emptyGraph() {
        _G = dg();
        assertEquals("Initial graph has vertices", 0, _G.vertexSize());
        assertEquals("Initial graph has edges", 0, _G.edgeSize());
    }

    /** Check that creating a directed graph yields right edges and
     *  vertices. */
    @Test(timeout = 1000)
    public void makeDirected() {
        fillGraph(dg(), NV1, E1);
        assertTrue("directedness", _G.isDirected());
        checkDirectedGraph("", asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), E1);
    }

    /** Check that creating an undirected graph yields right edges and
     *  vertices. */
    @Test(timeout = 1000)
    public void makeUndirected() {
        fillGraph(ug(), NV1, E1);
        assertFalse("undirectedness", _G.isDirected());
        checkUndirectedGraph("", asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), E1);
    }

    /** Check structure of directed graph with self edges. */
    @Test(timeout = 1000)
    public void makeDirectedSelf() {
        fillGraph(dg(), NV4, E4);
        assertTrue("directedness", _G.isDirected());
        checkDirectedGraph("", asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), E4);
    }

    /** Check structure of undirected graph with self edges. */
    @Test(timeout = 1000)
    public void makeUndirectedSelf() {
        fillGraph(ug(), NV4, E4);
        assertFalse("undirectedness", _G.isDirected());
        checkUndirectedGraph("", asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), E4);
    }

    /** Check that creating a directed graph and then removing a
     *  vertex yields right edges and vertices. */
    @Test(timeout = 1000)
    public void removeDirected() {
        fillGraph(dg(), NV1, E1);
        _G.remove(2);
        checkDirectedGraph("After removing 2: ",
                           asList(1, 3, 4, 5, 6, 7, 8, 9, 10), E2);
    }

    /** Check that creating an undirected graph and then removing a
     *  vertex yields right edges and vertices. */
    @Test(timeout = 1000)
    public void removeUndirected() {
        fillGraph(ug(), NV1, E1);
        _G.remove(2);
        checkUndirectedGraph("After removing 2: ",
                             asList(1, 3, 4, 5, 6, 7, 8, 9, 10), E2);
    }

    /** Check that creating a directed graph with self edges and then removing
     *  vertices yields right edges and vertices. */
    @Test(timeout = 1000)
    public void removeDirectedSelf() {
        fillGraph(dg(), NV4, E4);
        _G.remove(2);
        checkDirectedGraph("After removing 2: ",
                           asList(1, 3, 4, 5, 6, 7, 8, 9, 10), E5);
    }

    /** Check that creating an undirected graph and then removing a
     *  vertex yields right edges and vertices. */
    @Test(timeout = 1000)
    public void removeUndirectedSelf() {
        fillGraph(ug(), NV4, E4);
        _G.remove(2);
        checkUndirectedGraph("After removing 2: ",
                             asList(1, 3, 4, 5, 6, 7, 8, 9, 10), E5);
    }

    /** Check that new vertex numbers are always the smallest available positive
     *  integers. */
    @Test(timeout = 1000)
    public void addDelete() {
        _G = dg();
        int v;
        v = _G.add();
        assertEquals("Wrong vertex", 1, v);
        v = _G.add();
        assertEquals("Wrong vertex", 2, v);
        v = _G.add();
        assertEquals("Wrong vertex", 3, v);
        v = _G.add();
        _G.remove(1);
        _G.remove(3);
        _G.remove(2);
        v = _G.add();
        assertEquals("Wrong vertex after removals", 1, v);
        v = _G.add();
        assertEquals("Wrong vertex after removals", 2, v);
        v = _G.add();
        assertEquals("Wrong vertex after removals", 3, v);
        v = _G.add();
        assertEquals("Wrong vertex after removals", 5, v);
    }

    /** Test that maxVertex works. */
    @Test(timeout = 1000)
    public void maxVertex() {
        fillGraph(dg(), NV1, E1);
        assertEquals("Wrong maximum vertex for E1", NV1, _G.maxVertex());
        _G.remove(2);
        assertEquals("Wrong maximum vertex for E1 - { 2 }",
                     NV1, _G.maxVertex());
        _G.remove(NV1);
        assertEquals("Wrong maximum vertex for E1 - { 2, NV1 }",
                     NV1 - 1, _G.maxVertex());
        _G.add();
        assertEquals("Wrong maximum vertex for E1 - { 2, NV1 } "
                     + "U { new-vertex }",
                     NV1 - 1, _G.maxVertex());
        _G.add();
        assertEquals("Wrong maximum vertex for E1 - { 2, NV1 } "
                     + "U { new-vertex1 } U { new-vertex2 }",
                     NV1, _G.maxVertex());
    }

    /*===== Traversal Tests. =====*/

    private static class Traverse1 extends DepthFirstTraversal {

        Traverse1(Graph G) {
            super(G);
        }

        private ArrayList<Integer> _trail = new ArrayList<>();

        @Override
        protected boolean visit(int v) {
            _trail.add(v);
            return true;
        }

        @Override
        protected boolean postVisit(int v) {
            _trail.add(-v);
            return true;
        }

        ArrayList<Integer> trail() {
            return new ArrayList<>(_trail);
        }

    }

    /** Checking code for depth-first traversals. */
    private static class DFTCheck {

        private final BitSet _visited = new BitSet();
        private final ArrayList<ArrayList<Integer>> _succs;
        private ArrayList<Integer> _trail;
        private int _p;
        private String _messagePrefix;

        /** A traversal checker for the graph with successor edges
         *  SUCCS, which maps vertex numbers in the graph to lists
         *  of successor vertices.  */
        DFTCheck(String messagePrefix, ArrayList<ArrayList<Integer>> succs) {
            _succs = succs;
            _trail = null;
            _messagePrefix = messagePrefix;
        }

        /** Check that TRAIL is a valid depth-first traversal from V0
         *  of a directed graph with my successor map. Trail consists
         *  of a visit/post-visit sequence of vertices; positive
         *  vertices indicate visits, negative ones indicate post-
         *  visits. */
        void checkTraversal(int v0, ArrayList<Integer> trail) {
            try {
                _visited.clear();
                _trail = trail;
                _p = 0;
                assertTrue(fmt("%s: Traversal should have visited %d first",
                               _messagePrefix, v0),
                           _p < _trail.size() && v0 == _trail.get(0));
                check(v0);
            } catch (AssertionError excp) {
                report(_messagePrefix, _succs,
                       String.format("%nSequence traversed, where "
                                     + "'<v>' means post-visit v:%n"),
                       trail);
                throw excp;
            }
        }

        /** Continue check of the remainder of the trail at V,
         *  assuming that currently marked vertices have already been
         *  traversed.  End the check upon post-visit of V. */
        private void check(int v) {
            assertFalse(fmt("%s: Vertex %d visited twice",
                            _messagePrefix, v), _visited.get(v));
            _visited.set(v);
            _p += 1;

            ArrayList<Integer> succ = _succs.get(v);
            while (_p < _trail.size() && _trail.get(_p) > 0) {
                int w = _trail.get(_p);
                assertTrue(fmt("%s: Vertex %d visited out of sequence",
                               _messagePrefix, w),
                           succ.contains(w));
                check(w);
            }
            assertTrue(fmt("%s: Vertex %d not post-visited in proper sequence",
                           _messagePrefix, v),
                       _p < _trail.size() && v == -_trail.get(_p));
            for (int s : succ) {
                assertTrue(fmt("%s: Vertex %d post-visited before %d visited",
                               _messagePrefix, v, s),
                           _visited.get(s));
            }
            _p += 1;
        }
    }

    /** Test depth-first traversal on directed graph. */
    @Test(timeout = 1000)
    public void depthFirstDirectedTraverse1() {
        fillGraph(dg(), NV1, E1);

        Traverse1 traverse = new Traverse1(_G);
        traverse.traverse(1);
        DFTCheck checker = new DFTCheck("depthFirstDirectedTraversal1",
                                        successorEdges(E1, NV1));
        checker.checkTraversal(1, traverse.trail());
    }

    /** Test depth-first traversal on undirected graph. */
    @Test(timeout = 1000)
    public void depthFirstUndirectedTraverse1() {
        fillGraph(ug(), NV1, E1);

        Traverse1 traverse = new Traverse1(_G);
        traverse.traverse(1);
        DFTCheck checker = new DFTCheck("depthFirstUndirectedTraverse1",
                                        neighborEdges(E1, NV1));
        checker.checkTraversal(1, traverse.trail());
    }

    private class Traverse2 extends BreadthFirstTraversal {

        Traverse2(Graph G) {
            super(G);
        }

        private ArrayList<Integer> _trail = new ArrayList<>();

        @Override
        protected boolean visit(int v) {
            assertFalse(fmt("BFT: %d visited multiple time", v),
                        _trail.contains(v));
            _trail.add(v);
            return true;
        }

        ArrayList<Integer> trail() {
            return new ArrayList<>(_trail);
        }

    }

    /** Test breadth-first traversal on directed graph. */
    @Test(timeout = 1000)
    public void breadthFirstDirectedTraverse1() {
        fillGraph(dg(), NV3, E3);

        Traverse2 traverse = new Traverse2(_G);
        traverse.traverse(1);
        Map<Integer, Integer> levels =
            levelMap(successorEdges(E3, NV3), 1);
        try {
            assertTrue("wrong directed level order for breadth-first "
                       + "traversal",
                       levelOrdered(levels, traverse.trail()));
        } catch (AssertionError excp) {
            report("breadthFirstDirectedTraverse1",
                   successorEdges(E3, NV3),
                   "Visit sequence produced from vertex 1",
                   traverse.trail());
            System.err.printf("%n Level map: %s%n", levels);
            throw excp;
        }
    }

    /** Test breadth-first traversal on undirected graph. */
    @Test(timeout = 1000)
    public void breadthFirstUndirectedTraverse1() {
        fillGraph(ug(), NV3, E3);

        Traverse2 traverse = new Traverse2(_G);
        traverse.traverse(1);
        Map<Integer, Integer> levels =
            levelMap(neighborEdges(E3, NV3), 1);
        try {
            assertTrue("wrong undirected level order for breadth-first "
                       + " traversal",
                       levelOrdered(levels, traverse.trail()));
        } catch (AssertionError excp) {
            report("breadthFirstUndirectedTraverse1",
                   neighborEdges(E3, NV3),
                   "Visit sequence produced from vertex 1",
                   traverse.trail());
            System.err.printf("%n Level map: %s%n", levels);
            throw excp;
        }
    }

    /** Test traversal on directed graph with self edges. */
    @Test(timeout = 1000)
    public void depthFirstDirectedTraversalSelf() {
        fillGraph(dg(), NV4, E4);
        Traverse1 traverse = new Traverse1(_G);
        traverse.traverse(1);
        DFTCheck checker = new DFTCheck("depthFirstDirectedTraversalSelf",
                                        successorEdges(E4, NV4));
        checker.checkTraversal(1, traverse.trail());
    }

    /** Test traversal on undirected graph with self edges. */
    @Test(timeout = 1000)
    public void depthFirstUndirectedTraversalSelf() {
        fillGraph(ug(), NV4, E4);

        Traverse1 traverse = new Traverse1(_G);
        traverse.traverse(1);
        DFTCheck checker = new DFTCheck("depthFirstUndirectedTraversalSelf",
                                        neighborEdges(E4, NV4));
        checker.checkTraversal(1, traverse.trail());
    }

    private static class Edge extends Point {

        Edge(int x, int y) {
            super(x, y);
        }

        @Override
        public String toString() {
            return String.format("(%d, %d)", x, y);
        }
    }

    /** The test graph. */
    private Graph _G;

}
