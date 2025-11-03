package com.smartcity.graph.topo;

import com.smartcity.common.Graph;
import com.smartcity.common.Metrics;
import com.smartcity.common.MetricsImpl;

import java.util.*;

/**
 * Topological sorting using Kahn's algorithm (BFS-based).
 * Works only on DAGs (Directed Acyclic Graphs).
 */
public class TopologicalSort {
    private final Graph graph;
    private final Metrics metrics;

    /**
     * Create topological sorter
     * @param graph the directed acyclic graph
     */
    public TopologicalSort(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Graph must be directed");
        }
        this.graph = graph;
        this.metrics = new MetricsImpl();
    }

    /**
     * Perform topological sort using Kahn's algorithm
     * @return topologically sorted list of vertices, or null if cycle detected
     */
    public List<Integer> sort() {
        int n = graph.getN();
        int[] inDegree = new int[n];

        // Calculate in-degrees
        for (int u = 0; u < n; u++) {
            for (Graph.Edge e : graph.getNeighbors(u)) {
                inDegree[e.to]++;
            }
        }

        // Initialize queue with vertices having in-degree 0
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
                metrics.increment("queue_pushes");
            }
        }

        List<Integer> result = new ArrayList<>();

        metrics.startTimer();

        while (!queue.isEmpty()) {
            int u = queue.poll();
            metrics.increment("queue_pops");
            result.add(u);

            // Reduce in-degree of neighbors
            for (Graph.Edge e : graph.getNeighbors(u)) {
                int v = e.to;
                metrics.increment("edges_processed");
                inDegree[v]--;

                if (inDegree[v] == 0) {
                    queue.offer(v);
                    metrics.increment("queue_pushes");
                }
            }
        }

        metrics.stopTimer();

        // Check if all vertices were processed (no cycle)
        if (result.size() != n) {
            return null; // Cycle detected
        }

        return result;
    }

    /**
     * DFS-based topological sort (alternative implementation)
     * @return topologically sorted list of vertices, or null if cycle detected
     */
    public List<Integer> sortDFS() {
        int n = graph.getN();
        boolean[] visited = new boolean[n];
        boolean[] recStack = new boolean[n];
        Stack<Integer> stack = new Stack<>();

        metrics.reset();
        metrics.startTimer();

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                if (dfsHelper(i, visited, recStack, stack)) {
                    metrics.stopTimer();
                    return null; // Cycle detected
                }
            }
        }

        metrics.stopTimer();

        // Pop all vertices from stack
        List<Integer> result = new ArrayList<>();
        while (!stack.isEmpty()) {
            result.add(stack.pop());
        }

        return result;
    }

    /**
     * Helper for DFS-based topological sort
     * @return true if cycle detected
     */
    private boolean dfsHelper(int u, boolean[] visited, boolean[] recStack, Stack<Integer> stack) {
        visited[u] = true;
        recStack[u] = true;
        metrics.increment("dfs_visits");

        for (Graph.Edge e : graph.getNeighbors(u)) {
            int v = e.to;
            metrics.increment("edges_explored");

            if (!visited[v]) {
                if (dfsHelper(v, visited, recStack, stack)) {
                    return true; // Cycle detected
                }
            } else if (recStack[v]) {
                return true; // Back edge, cycle detected
            }
        }

        recStack[u] = false;
        stack.push(u);
        return false;
    }

    /**
     * Get metrics from the last execution
     */
    public Metrics getMetrics() {
        return metrics;
    }

    /**
     * Expand SCC topological order to original vertices
     * @param sccOrder topological order of SCCs
     * @param sccs list of SCCs
     * @return topological order of original vertices
     */
    public static List<Integer> expandSCCOrder(List<Integer> sccOrder, List<List<Integer>> sccs) {
        List<Integer> expanded = new ArrayList<>();
        for (int sccIdx : sccOrder) {
            expanded.addAll(sccs.get(sccIdx));
        }
        return expanded;
    }
}