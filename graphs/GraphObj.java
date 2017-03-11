package graph;

import java.util.ArrayList;
/* See restrictions in Graph.java. */

/** A partial implementation of Graph containing elements common to
 *  directed and undirected graphs.
 *
 *  @author Jacky Tian
 */
abstract class GraphObj extends Graph {

    /** An ArrayList containing all vertices in the graph. */
    private ArrayList<Integer> _vertices;

    /** An ArrayList containing edges incident on two vertices. */
    private ArrayList<int[]> _edges;

    /** A new, empty Graph. */
    GraphObj() {
        _vertices = new ArrayList<Integer>();
        _edges = new ArrayList<int[]>();
    }

    @Override
    public int vertexSize() {
        return _vertices.size();
    }

    @Override
    public int maxVertex() {
        return _vertices.get(_vertices.size() - 1);
    }

    @Override
    public int edgeSize() {
        return _edges.size();
    }

    @Override
    public abstract boolean isDirected();

    @Override
    public int outDegree(int v) {
        int count = 0;
        for (int i = 0; i < _edges.size(); i += 1) {
            if (_edges.get(i)[0] == v) {
                count += 1;
            } else if (!isDirected()) {
                if (_edges.get(i)[1] == v) {
                    count += 1;
                }
            }
        }
        return count;
    }

    @Override
    public abstract int inDegree(int v);

    @Override
    public boolean contains(int u) {
        for (int i = 0; i < _vertices.size(); i += 1) {
            if (_vertices.get(i) == u) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(int u, int v) {
        if (!contains(u) || !contains(v)) {
            return false;
        }
        for (int i = 0; i < _edges.size(); i += 1) {
            if (_edges.get(i)[0] == u && _edges.get(i)[1] == v) {
                return true;
            }
            if (!isDirected()) {
                if (_edges.get(i)[0] == v && _edges.get(i)[1] == u) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int add() {
        int index = 0;
        while (index < vertexSize() && _vertices.get(index) == index + 1) {
            index += 1;
        }
        _vertices.add(index, index + 1);
        return index + 1;
    }

    @Override
    public int add(int u, int v) {
        checkMyVertex(u);
        checkMyVertex(v);
        for (int i = 0; i < _edges.size(); i += 1) {
            if (_edges.get(i)[0] == u && _edges.get(i)[1] == v) {
                return edgeId(u, v);
            }
        }
        int[] edge = new int[2];
        edge[0] = u;
        edge[1] = v;
        _edges.add(edge);
        return edgeId(u, v);
    }

    @Override
    public void remove(int v) {
        int i = 0;
        while (i < _vertices.size()) {
            if (_vertices.get(i) == v) {
                _vertices.remove(i);
            } else {
                i += 1;
            }
        }
        i = 0;
        while (i < _edges.size()) {
            if (_edges.get(i)[0] == v || _edges.get(i)[1] == v) {
                _edges.remove(i);
            } else {
                i += 1;
            }
        }
    }

    @Override
    public void remove(int u, int v) {
        for (int i = 0; i < _edges.size(); i += 1) {
            if (_edges.get(i)[0] == u && _edges.get(i)[1] == v) {
                _edges.remove(i);
            }
        }
    }

    @Override
    public Iteration<Integer> vertices() {
        return Iteration.iteration(_vertices.iterator());
    }

    @Override
    public int successor(int v, int k) {
        int count = k;
        for (int i = 0; i < _edges.size(); i += 1) {
            if (_edges.get(i)[0] == v) {
                if (count == 0) {
                    return _edges.get(i)[1];
                } else {
                    count -= 1;
                }
            } else if (_edges.get(i)[1] == v && !isDirected()) {
                if (count == 0) {
                    return _edges.get(i)[0];
                } else {
                    count -= 1;
                }
            }
        }
        return 0;
    }

    @Override
    public abstract int predecessor(int v, int k);

    @Override
    public Iteration<Integer> successors(int v) {
        ArrayList<Integer> s = new ArrayList<Integer>();
        for (int i = 0; i < outDegree(v); i += 1) {
            s.add(successor(v, i));
        }
        return Iteration.iteration(s.iterator());
    }

    @Override
    public abstract Iteration<Integer> predecessors(int v);

    @Override
    public Iteration<int[]> edges() {
        return Iteration.iteration(_edges.iterator());
    }

    @Override
    protected void checkMyVertex(int v) {
        if (!contains(v)) {
            throw new IllegalArgumentException("vertex not from Graph");
        }
    }

    @Override
    protected int edgeId(int u, int v) {
        return (u + v) * (u + v + 1) / 2 + v;
    }

    /** Returns my vertices. */
    public ArrayList<Integer> getVertices() {
        return _vertices;
    }

    /** Returns my edges. */
    public ArrayList<int[]> getEdges() {
        return _edges;
    }

}
