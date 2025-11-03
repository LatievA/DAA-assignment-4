package com.smartcity.graph.dagsp;

import com.smartcity.common.Graph;
import com.smartcity.common.Metrics;
import com.smartcity.common.MetricsImpl;
import com.smartcity.graph.topo.TopologicalSort;

import java.util.*;

/**
 * Shortest and longest path algorithms for DAGs.
 * Uses topological ordering for O(V+E) time complexity.
 */
public class DAGShortestPath {
    private final Graph graph;
    private final Metrics metrics;

    /**
     * Result of shortest/longest path computation
     */
    public static class PathResult {
        public final int[] dist;
        public final int[] parent;
        public final List<Integer> topoOrder;

        public PathResult(int[] dist, int[] parent, List<Integer> topoOrder) {
            this.dist = dist;
            this.parent = parent;
            this.topoOrder = topoOrder;
        }

        /**
         * Reconstruct path from source to target
         */
        public List<Integer> reconstructPath(int source, int target) {
            if (dist[target] == Integer.MAX_VALUE || dist[target] == Integer.MIN_VALUE) {
                return null; // No path exists
            }

            List<Integer> path = new ArrayList<>();
            int current = target;

            while (current != -1) {
                path.add(current);
                current = parent[current];
            }

            Collections.reverse(path);
            return path;
        }
    }

    /**
     * Create DAG shortest path solver
     * @param graph the directed acyclic graph
     */
    public DAGShortestPath(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Graph must be directed");
        }
        this.graph = graph;
        this.metrics = new MetricsImpl();
    }

    /**
     * Compute shortest paths from source to all vertices
     * @param source the source vertex
     * @return PathResult containing distances and parent pointers
     */
    public PathResult shortestPaths(int source) {
        int n = graph.getN();

        // Get topological order
        TopologicalSort topoSort = new TopologicalSort(graph);
        List<Integer> topoOrder = topoSort.sort();

        if (topoOrder == null) {
            throw new IllegalStateException("Graph contains a cycle");
        }

        // Initialize distances and parents
        int[] dist = new int[n];
        int[] parent = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        metrics.startTimer();

        // Process vertices in topological order
        for (int u : topoOrder) {
            if (dist[u] != Integer.MAX_VALUE) {
                for (Graph.Edge e : graph.getNeighbors(u)) {
                    int v = e.to;
                    metrics.increment("relaxations");

                    // Relaxation
                    if (dist[u] + e.weight < dist[v]) {
                        dist[v] = dist[u] + e.weight;
                        parent[v] = u;
                        metrics.increment("updates");
                    }
                }
            }
        }

        metrics.stopTimer();

        return new PathResult(dist, parent, topoOrder);
    }

    /**
     * Compute longest paths from source to all vertices
     * Uses negation technique: longest path = -shortest path with negated weights
     * @param source the source vertex
     * @return PathResult containing distances and parent pointers
     */
    public PathResult longestPaths(int source) {
        int n = graph.getN();

        // Get topological order
        TopologicalSort topoSort = new TopologicalSort(graph);
        List<Integer> topoOrder = topoSort.sort();

        if (topoOrder == null) {
            throw new IllegalStateException("Graph contains a cycle");
        }

        // Initialize distances and parents
        int[] dist = new int[n];
        int[] parent = new int[n];
        Arrays.fill(dist, Integer.MIN_VALUE);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        metrics.startTimer();

        // Process vertices in topological order
        for (int u : topoOrder) {
            if (dist[u] != Integer.MIN_VALUE) {
                for (Graph.Edge e : graph.getNeighbors(u)) {
                    int v = e.to;
                    metrics.increment("relaxations");

                    // Relaxation for longest path (use max instead of min)
                    if (dist[u] + e.weight > dist[v]) {
                        dist[v] = dist[u] + e.weight;
                        parent[v] = u;
                        metrics.increment("updates");
                    }
                }
            }
        }

        metrics.stopTimer();

        return new PathResult(dist, parent, topoOrder);
    }

    /**
     * Find the critical path (longest path in the DAG)
     * @param source the source vertex
     * @return CriticalPathResult with path and length
     */
    public CriticalPathResult findCriticalPath(int source) {
        PathResult result = longestPaths(source);

        // Find the vertex with maximum distance
        int maxDist = Integer.MIN_VALUE;
        int maxVertex = -1;

        for (int i = 0; i < result.dist.length; i++) {
            if (result.dist[i] != Integer.MIN_VALUE && result.dist[i] > maxDist) {
                maxDist = result.dist[i];
                maxVertex = i;
            }
        }

        if (maxVertex == -1) {
            return new CriticalPathResult(Collections.singletonList(source), 0);
        }

        List<Integer> path = result.reconstructPath(source, maxVertex);
        return new CriticalPathResult(path, maxDist);
    }

    /**
     * Result of critical path computation
     */
    public static class CriticalPathResult {
        public final List<Integer> path;
        public final int length;

        public CriticalPathResult(List<Integer> path, int length) {
            this.path = path;
            this.length = length;
        }

        @Override
        public String toString() {
            return String.format("Critical Path: %s, Length: %d", path, length);
        }
    }

    /**
     * Get metrics from the last execution
     */
    public Metrics getMetrics() {
        return metrics;
    }
}