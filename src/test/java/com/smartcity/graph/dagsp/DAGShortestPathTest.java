package com.smartcity.graph.dagsp;

import com.smartcity.common.Graph;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Unit tests for DAG Shortest Path algorithms
 */
class DAGShortestPathTest {

    @Test
    void testSimpleShortestPath() {
        // Linear: 0 --(5)-> 1 --(3)-> 2
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 5);
        g.addEdge(1, 2, 3);

        DAGShortestPath sp = new DAGShortestPath(g);
        DAGShortestPath.PathResult result = sp.shortestPaths(0);

        assertEquals(0, result.dist[0]);
        assertEquals(5, result.dist[1]);
        assertEquals(8, result.dist[2]);

        List<Integer> path = result.reconstructPath(0, 2);
        assertNotNull(path);
        assertEquals(3, path.size());
        assertEquals(0, path.get(0).intValue());
        assertEquals(1, path.get(1).intValue());
        assertEquals(2, path.get(2).intValue());
    }

    @Test
    void testMultiplePathsShortestWins() {
        // Diamond with different weights
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1);  // 0->1->3 = 1+1 = 2
        g.addEdge(0, 2, 5);  // 0->2->3 = 5+1 = 6
        g.addEdge(1, 3, 1);
        g.addEdge(2, 3, 1);

        DAGShortestPath sp = new DAGShortestPath(g);
        DAGShortestPath.PathResult result = sp.shortestPaths(0);

        assertEquals(0, result.dist[0]);
        assertEquals(1, result.dist[1]);
        assertEquals(5, result.dist[2]);
        assertEquals(2, result.dist[3]); // Via 0->1->3
    }

    @Test
    void testLongestPath() {
        // Same graph, but find longest path
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 2, 5);
        g.addEdge(1, 3, 1);
        g.addEdge(2, 3, 1);

        DAGShortestPath sp = new DAGShortestPath(g);
        DAGShortestPath.PathResult result = sp.longestPaths(0);

        assertEquals(0, result.dist[0]);
        assertEquals(1, result.dist[1]);
        assertEquals(5, result.dist[2]);
        assertEquals(6, result.dist[3]); // Via 0->2->3
    }

    @Test
    void testCriticalPath() {
        // Project scheduling scenario
        Graph g = new Graph(5, true);
        g.addEdge(0, 1, 3);
        g.addEdge(0, 2, 6);
        g.addEdge(1, 3, 2);
        g.addEdge(2, 3, 1);
        g.addEdge(3, 4, 4);

        DAGShortestPath sp = new DAGShortestPath(g);
        DAGShortestPath.CriticalPathResult critical = sp.findCriticalPath(0);

        assertNotNull(critical.path);
        assertEquals(11, critical.length); // 0->2->3->4 = 6+1+4
    }

    @Test
    void testUnreachableVertices() {
        // Graph with unreachable vertex
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 5);
        g.addEdge(2, 3, 3);

        DAGShortestPath sp = new DAGShortestPath(g);
        DAGShortestPath.PathResult result = sp.shortestPaths(0);

        assertEquals(0, result.dist[0]);
        assertEquals(5, result.dist[1]);
        assertEquals(Integer.MAX_VALUE, result.dist[2]);
        assertEquals(Integer.MAX_VALUE, result.dist[3]);

        List<Integer> path = result.reconstructPath(0, 3);
        assertNull(path); // No path exists
    }

    @Test
    void testSingleVertex() {
        Graph g = new Graph(1, true);

        DAGShortestPath sp = new DAGShortestPath(g);
        DAGShortestPath.PathResult result = sp.shortestPaths(0);

        assertEquals(0, result.dist[0]);
    }

    @Test
    void testZeroWeights() {
        // Graph with zero-weight edges
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 0);
        g.addEdge(1, 2, 0);

        DAGShortestPath sp = new DAGShortestPath(g);
        DAGShortestPath.PathResult result = sp.shortestPaths(0);

        assertEquals(0, result.dist[0]);
        assertEquals(0, result.dist[1]);
        assertEquals(0, result.dist[2]);
    }

    @Test
    void testComplexDAG() {
        // More complex scheduling scenario
        Graph g = new Graph(6, true);
        g.addEdge(0, 1, 2);
        g.addEdge(0, 2, 3);
        g.addEdge(1, 3, 4);
        g.addEdge(2, 3, 1);
        g.addEdge(2, 4, 5);
        g.addEdge(3, 5, 2);
        g.addEdge(4, 5, 1);

        DAGShortestPath sp = new DAGShortestPath(g);

        // Shortest paths
        DAGShortestPath.PathResult shortest = sp.shortestPaths(0);
        assertEquals(0, shortest.dist[0]);
        assertEquals(2, shortest.dist[1]);
        assertEquals(3, shortest.dist[2]);
        assertEquals(4, shortest.dist[3]); // via 0->2->3
        assertEquals(8, shortest.dist[4]); // via 0->2->4
        assertEquals(6, shortest.dist[5]); // via 0->2->3->5

        // Longest path (critical path)
        DAGShortestPath.CriticalPathResult critical = sp.findCriticalPath(0);
        assertEquals(9, critical.length); // 0->2->4->5 = 3+5+1
    }
}