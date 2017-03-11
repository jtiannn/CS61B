package graph;

/* See restrictions in Graph.java. */

import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Comparator;

/** The shortest paths through an edge-weighted graph.
 *  By overrriding methods getWeight, setWeight, getPredecessor, and
 *  setPredecessor, the client can determine how to represent the weighting
 *  and the search results.  By overriding estimatedDistance, clients
 *  can search for paths to specific destinations using A* search.
 *  @author Jacky Tian
 */
public abstract class ShortestPaths {

    /** The shortest paths in G from SOURCE. */
    public ShortestPaths(Graph G, int source) {
        this(G, source, 0);
    }

    /** A shortest path in G from SOURCE to DEST. */
    public ShortestPaths(Graph G, int source, int dest) {
        _G = G;
        _source = source;
        _dest = dest;
        _fringe = new TreeSet<Integer>(new WeightCompare());
    }

    /** Initialize the shortest paths.  Must be called before using
     *  getWeight, getPredecessor, and pathTo. */
    public void setPaths() {
        setWeight(_source, 0);
        _fringe.add(_source);
        while (!_fringe.isEmpty()) {
            int next = _fringe.pollFirst();
            if (next == _dest) {
                break;
            }
            for (int i = 0; i < _G.outDegree(next); i += 1) {
                int s = _G.successor(next, i);
                if (getWeight(s) > getWeight(next, s) + getWeight(next)) {
                    setWeight(s, getWeight(next, s) + getWeight(next));
                    setPredecessor(s, next);
                    _fringe.remove(s);
                    _fringe.add(s);
                }
            }
        }
    }

    /** Returns the starting vertex. */
    public int getSource() {
        return _source;
    }

    /** Returns the target vertex, or 0 if there is none. */
    public int getDest() {
        return _dest;
    }

    /** Returns the current weight of vertex V in the graph.  If V is
     *  not in the graph, returns positive infinity. */
    public abstract double getWeight(int v);

    /** Set getWeight(V) to W. Assumes V is in the graph. */
    protected abstract void setWeight(int v, double w);

    /** Returns the current predecessor vertex of vertex V in the graph, or 0 if
     *  V is not in the graph or has no predecessor. */
    public abstract int getPredecessor(int v);

    /** Set getPredecessor(V) to U. */
    protected abstract void setPredecessor(int v, int u);

    /** Returns an estimated heuristic weight of the shortest path from vertex
     *  V to the destination vertex (if any).  This is assumed to be less
     *  than the actual weight, and is 0 by default. */
    protected double estimatedDistance(int v) {
        return 0.0;
    }

    /** Returns the current weight of edge (U, V) in the graph.  If (U, V) is
     *  not in the graph, returns positive infinity. */
    protected abstract double getWeight(int u, int v);

    /** Returns a list of vertices starting at _source and ending
     *  at V that represents a shortest path to V.  Invalid if there is a
     *  destination vertex other than V. */
    public List<Integer> pathTo(int v) {
        ArrayList<Integer> path = new ArrayList<Integer>();
        path.add(v);
        int vertex = v;
        while (vertex != _source) {
            path.add(0, getPredecessor(vertex));
            vertex = getPredecessor(vertex);
        }
        return path;
    }

    /** Returns a list of vertices starting at the source and ending at the
     *  destination vertex. Invalid if the destination is not specified. */
    public List<Integer> pathTo() {
        return pathTo(getDest());
    }

    /** The graph being searched. */
    protected final Graph _G;
    /** The starting vertex. */
    private final int _source;
    /** The target vertex. */
    private final int _dest;
    /** The fringe. */
    protected TreeSet<Integer> _fringe;
    /** An ordering of edges by weight. */
    private class WeightCompare implements Comparator<Integer> {
        /** Overrides compare method. */
        @Override
        public int compare(Integer e0, Integer e1) {
            if (getWeight(e0) + estimatedDistance(e0)
                == getWeight(e1) + estimatedDistance(e1)) {
                if (e0 < e1) {
                    return -1;
                } else if (e0 > e1) {
                    return 1;
                } else {
                    return 0;
                }
            } else {
                if (getWeight(e0) + estimatedDistance(e0)
                    < getWeight(e1) + estimatedDistance(e1)) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }

}
