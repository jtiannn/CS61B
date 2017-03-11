package graph;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Iterator;

/** Unit tests for the Graph class.
 *  @author Jacky Tian
 */
public class GraphTesting {

    public class TestPaths extends SimpleShortestPaths {
        /** Constructor. */
        public TestPaths(Graph G, int source, int dest) {
            super(G, source, dest);
        }

        /** Returns the current weight of edge (U, V) in the graph.  If (U, V)
         *  is not in the graph, returns positive infinity. */
        @Override
        protected double getWeight(int u, int v) {
            if (u == 2 || v == 2) {
                return 1;
            } else if (u == 3 || v == 3) {
                return 4;
            } else {
                return 10;
            }
        }
    }

    @Test
    public void shortestPath() {
        UndirectedGraph undirected = new UndirectedGraph();
        undirected.add();
        undirected.add();
        undirected.add();
        undirected.add();
        undirected.add(1, 2);
        undirected.add(1, 3);
        undirected.add(1, 4);
        undirected.add(2, 3);
        undirected.add(3, 4);
        TestPaths paths = new TestPaths(undirected, 1, 4);
        paths.setPaths();
        Iterator<Integer> path = paths.pathTo().iterator();
        int next = path.next();
        assertEquals(1, next);
        next = path.next();
        assertEquals(2, next);
        next = path.next();
        assertEquals(3, next);
        next = path.next();
        assertEquals(4, next);
    }

    @Test
    public void directedGraph() {
        DirectedGraph directed = new DirectedGraph();
        directed.add();
        directed.add();
        directed.add();
        directed.add();
        directed.remove(2);
        assertFalse(directed.contains(2));
        assertEquals("Number of vertices", 3, directed.vertexSize());
        assertEquals("Largest Vertex", 4, directed.maxVertex());
        directed.add();
        assertTrue(directed.contains(2));
        assertEquals("Number of vertices", 4, directed.vertexSize());
        directed.add(1, 2);
        directed.add(2, 3);
        directed.add(3, 4);
        directed.add(4, 1);
        directed.add(1, 3);
        assertEquals("Number of edges", 5, directed.edgeSize());
        assertEquals("Number of out edges", 2, directed.outDegree(1));
        assertEquals("Number of in edges", 1, directed.inDegree(1));
        assertFalse(directed.contains(2, 1));
        assertTrue(directed.contains(1, 2));

        Iteration<Integer> s = directed.successors(1);
        int next = s.next();
        assertEquals(2, next);
        next = s.next();
        assertEquals(3, next);
        assertFalse(s.hasNext());

        Iteration<Integer> p = directed.predecessors(1);
        next = p.next();
        assertEquals(4, next);
        assertFalse(p.hasNext());

        directed.remove(1, 2);
        assertEquals(4, directed.edgeSize());
    }

    @Test
    public void undirectedGraph() {
        UndirectedGraph undirected = new UndirectedGraph();
        undirected.add();
        undirected.add();
        undirected.add();
        undirected.add();
        undirected.add(1, 2);
        undirected.add(2, 3);
        undirected.add(3, 4);
        undirected.add(4, 1);
        undirected.add(1, 3);
        assertEquals(3, undirected.inDegree(1));
        assertEquals(3, undirected.inDegree(3));

        Iteration<Integer> p = undirected.predecessors(1);
        int next = p.next();
        assertEquals(2, next);
        next = p.next();
        assertEquals(4, next);
        next = p.next();
        assertEquals(3, next);
        assertFalse(p.hasNext());
    }

    @Test
    public void emptyGraph() {
        DirectedGraph g = new DirectedGraph();
        assertEquals("Initial graph has vertices", 0, g.vertexSize());
        assertEquals("Initial graph has edges", 0, g.edgeSize());
    }

}
