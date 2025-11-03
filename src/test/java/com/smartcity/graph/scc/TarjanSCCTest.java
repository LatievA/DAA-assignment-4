package com.smartcity.graph.scc;

import com.smartcity.common.Graph;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Unit tests for Tarjan's SCC algorithm
 */
class TarjanSCCTest {

    @Test
    void testSimpleCycle() {
        // Graph: 0 -> 1 -> 2 -> 0 (one SCC)
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);

        TarjanSCC scc = new TarjanSCC(g);
        List<List<Integer>> sccs = scc.findSCCs();

        assertEquals(1, sccs.size());
        assertEquals(3, sccs.get(0).size());
    }

    @Test
    void testTwoSCCs() {
        // Graph: (0 <-> 1) -> (2 <-> 3)
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 0, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 3, 1);
        g.addEdge(3, 2, 1);

        TarjanSCC scc = new TarjanSCC(g);
        List<List<Integer>> sccs = scc.findSCCs();

        assertEquals(2, sccs.size());
    }

    @Test
    void testDAG() {
        // Pure DAG: 0 -> 1 -> 2 -> 3
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 3, 1);

        TarjanSCC scc = new TarjanSCC(g);
        List<List<Integer>> sccs = scc.findSCCs();

        // Each vertex is its own SCC
        assertEquals(4, sccs.size());
        for (List<Integer> component : sccs) {
            assertEquals(1, component.size());
        }
    }

    @Test
    void testSingleVertex() {
        Graph g = new Graph(1, true);

        TarjanSCC scc = new TarjanSCC(g);
        List<List<Integer>> sccs = scc.findSCCs();

        assertEquals(1, sccs.size());
        assertEquals(1, sccs.get(0).size());
    }

    @Test
    void testSelfLoop() {
        // Vertex with self-loop
        Graph g = new Graph(2, true);
        g.addEdge(0, 0, 1);
        g.addEdge(0, 1, 1);

        TarjanSCC scc = new TarjanSCC(g);
        List<List<Integer>> sccs = scc.findSCCs();

        assertEquals(2, sccs.size());
    }

    @Test
    void testCondensationGraph() {
        // Graph: (0 -> 1 -> 2 -> 1) -> 3
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 2);
        g.addEdge(2, 1, 1);
        g.addEdge(2, 3, 3);

        TarjanSCC scc = new TarjanSCC(g);
        List<List<Integer>> sccs = scc.findSCCs();

        assertEquals(3, sccs.size()); // {0}, {1,2}, {3}

        Graph condensation = scc.buildCondensationGraph();
        assertEquals(3, condensation.getN());
        assertTrue(condensation.getEdgeCount() >= 2);
    }

    @Test
    void testDisconnectedGraph() {
        // Two disconnected cycles
        Graph g = new Graph(6, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);
        g.addEdge(3, 4, 1);
        g.addEdge(4, 5, 1);
        g.addEdge(5, 3, 1);

        TarjanSCC scc = new TarjanSCC(g);
        List<List<Integer>> sccs = scc.findSCCs();

        assertEquals(2, sccs.size());
        assertEquals(3, sccs.get(0).size());
        assertEquals(3, sccs.get(1).size());
    }
}