package com.smartcity.graph.topo;

import com.smartcity.common.Graph;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Unit tests for Topological Sort
 */
class TopologicalSortTest {

    @Test
    void testLinearDAG() {
        // 0 -> 1 -> 2 -> 3
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 3, 1);

        TopologicalSort ts = new TopologicalSort(g);
        List<Integer> order = ts.sort();

        assertNotNull(order);
        assertEquals(4, order.size());

        // Verify order: 0 must come before 1, 1 before 2, etc.
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(1) < order.indexOf(2));
        assertTrue(order.indexOf(2) < order.indexOf(3));
    }

    @Test
    void testDiamondDAG() {
        // Diamond: 0 -> {1,2} -> 3
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 2, 1);
        g.addEdge(1, 3, 1);
        g.addEdge(2, 3, 1);

        TopologicalSort ts = new TopologicalSort(g);
        List<Integer> order = ts.sort();

        assertNotNull(order);
        assertEquals(4, order.size());

        // 0 must be first, 3 must be last
        assertEquals(0, order.get(0).intValue());
        assertEquals(3, order.get(3).intValue());
    }

    @Test
    void testCycleDetection() {
        // Cycle: 0 -> 1 -> 2 -> 0
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);

        TopologicalSort ts = new TopologicalSort(g);
        List<Integer> order = ts.sort();

        assertNull(order); // Should detect cycle
    }

    @Test
    void testSingleVertex() {
        Graph g = new Graph(1, true);

        TopologicalSort ts = new TopologicalSort(g);
        List<Integer> order = ts.sort();

        assertNotNull(order);
        assertEquals(1, order.size());
        assertEquals(0, order.get(0).intValue());
    }

    @Test
    void testDisconnectedDAG() {
        // Two separate chains: 0->1 and 2->3
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1);
        g.addEdge(2, 3, 1);

        TopologicalSort ts = new TopologicalSort(g);
        List<Integer> order = ts.sort();

        assertNotNull(order);
        assertEquals(4, order.size());

        // Verify partial orders
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(2) < order.indexOf(3));
    }

    @Test
    void testDFSVariant() {
        // Test DFS-based topological sort
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 2, 1);
        g.addEdge(1, 3, 1);
        g.addEdge(2, 3, 1);

        TopologicalSort ts = new TopologicalSort(g);
        List<Integer> order = ts.sortDFS();

        assertNotNull(order);
        assertEquals(4, order.size());

        // Verify order
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(0) < order.indexOf(2));
        assertTrue(order.indexOf(1) < order.indexOf(3));
        assertTrue(order.indexOf(2) < order.indexOf(3));
    }

    @Test
    void testDFSCycleDetection() {
        // Cycle detection with DFS variant
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);

        TopologicalSort ts = new TopologicalSort(g);
        List<Integer> order = ts.sortDFS();

        assertNull(order);
    }

    @Test
    void testComplexDAG() {
        // More complex DAG
        Graph g = new Graph(6, true);
        g.addEdge(5, 2, 1);
        g.addEdge(5, 0, 1);
        g.addEdge(4, 0, 1);
        g.addEdge(4, 1, 1);
        g.addEdge(2, 3, 1);
        g.addEdge(3, 1, 1);

        TopologicalSort ts = new TopologicalSort(g);
        List<Integer> order = ts.sort();

        assertNotNull(order);
        assertEquals(6, order.size());

        // Verify some dependencies
        assertTrue(order.indexOf(5) < order.indexOf(2));
        assertTrue(order.indexOf(5) < order.indexOf(0));
        assertTrue(order.indexOf(2) < order.indexOf(3));
        assertTrue(order.indexOf(3) < order.indexOf(1));
    }
}