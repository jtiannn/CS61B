package graph;

import java.util.ArrayList;

/* See restrictions in Graph.java. */

/** Represents a general unlabeled directed graph whose vertices are denoted by
 *  positive integers. Graphs may have self edges.
 *
 *  @author Jacky Tian
 */
public class DirectedGraph extends GraphObj {

    @Override
    public boolean isDirected() {
        return true;
    }

    @Override
    public int inDegree(int v) {
        int count = 0;
        for (int i = 0; i < getEdges().size(); i += 1) {
            if (getEdges().get(i)[1] == v) {
                count += 1;
            }
        }
        return count;
    }

    @Override
    public int predecessor(int v, int k) {
        int count = k;
        for (int i = 0; i < getEdges().size(); i += 1) {
            if (getEdges().get(i)[1] == v) {
                if (count == 0) {
                    return getEdges().get(i)[0];
                } else {
                    count -= 1;
                }
            }
        }
        return 0;
    }

    @Override
    public Iteration<Integer> predecessors(int v) {
        ArrayList<Integer> p = new ArrayList<Integer>();
        for (int i = 0; i < inDegree(v); i += 1) {
            p.add(predecessor(v, i));
        }
        return Iteration.iteration(p.iterator());
    }

}
