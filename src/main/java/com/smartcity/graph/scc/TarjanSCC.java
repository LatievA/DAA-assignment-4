package com.smartcity.graph.scc;

import com.smartcity.common.Graph;
import com.smartcity.common.Metrics;
import com.smartcity.common.MetricsImpl;

import java.util.*;

/**
 * Tarjan's algorithm for finding Strongly Connected Components.
 * Uses a single DFS pass with discovery time and low-link values.
 */
public class TarjanSCC {
    private final Graph graph;
    private final Metrics metrics;

    private int[] disc;      // Discovery time
    private int[] low;       // Low-link value
    private boolean[] onStack;
    private Stack<Integer> stack;
    private int time;
    private List<List<Integer>> sccs;

    /**
     * Create Tarjan SCC finder
     * @param graph the directed graph
     */
    public TarjanSCC(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Graph must be directed");
        }
        this.graph = graph;
        this.metrics = new MetricsImpl();
    }

    /**
     * Find all strongly connected components
     * @return list of SCCs, each SCC is a list of vertex indices
     */
    public List<List<Integer>> findSCCs() {
        int n = graph.getN();
        disc = new int[n];
        low = new int[n];
        onStack = new boolean[n];
        stack = new Stack<>();
        sccs = new ArrayList<>();
        time = 0;

        Arrays.fill(disc, -1);

        metrics.startTimer();

        // Run DFS from all unvisited vertices
        for (int i = 0; i < n; i++) {
            if (disc[i] == -1) {
                dfs(i);
            }
        }

        metrics.stopTimer();

        return sccs;
    }

    /**
     * DFS traversal for Tarjan's algorithm
     */
    private void dfs(int u) {
        metrics.increment("dfs_visits");

        // Initialize discovery time and low-link
        disc[u] = low[u] = time++;
        stack.push(u);
        onStack[u] = true;

        // Visit all neighbors
        for (Graph.Edge e : graph.getNeighbors(u)) {
            int v = e.to;
            metrics.increment("edges_explored");

            if (disc[v] == -1) {
                // Tree edge
                dfs(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                // Back edge to vertex in current SCC
                low[u] = Math.min(low[u], disc[v]);
            }
        }

        // If u is a root node, pop the stack and create SCC
        if (low[u] == disc[u]) {
            List<Integer> scc = new ArrayList<>();
            int v;
            do {
                v = stack.pop();
                onStack[v] = false;
                scc.add(v);
                metrics.increment("stack_pops");
            } while (v != u);

            sccs.add(scc);
            metrics.increment("sccs_found");
        }
    }

    /**
     * Get metrics from the last execution
     */
    public Metrics getMetrics() {
        return metrics;
    }

    /**
     * Create a mapping from vertex to its SCC index
     */
    public int[] getVertexToSCCMapping() {
        int[] mapping = new int[graph.getN()];
        for (int i = 0; i < sccs.size(); i++) {
            for (int v : sccs.get(i)) {
                mapping[v] = i;
            }
        }
        return mapping;
    }

    /**
     * Build the condensation graph (DAG of SCCs)
     * @return condensation graph where each node is an SCC
     */
    public Graph buildCondensationGraph() {
        int numSCCs = sccs.size();
        Graph condensation = new Graph(numSCCs, true);
        int[] vertexToSCC = getVertexToSCCMapping();

        Set<String> addedEdges = new HashSet<>();

        for (int u = 0; u < graph.getN(); u++) {
            int sccU = vertexToSCC[u];

            for (Graph.Edge e : graph.getNeighbors(u)) {
                int v = e.to;
                int sccV = vertexToSCC[v];

                // Add edge between different SCCs
                if (sccU != sccV) {
                    String edgeKey = sccU + "->" + sccV;
                    if (!addedEdges.contains(edgeKey)) {
                        condensation.addEdge(sccU, sccV, e.weight);
                        addedEdges.add(edgeKey);
                    }
                }
            }
        }

        return condensation;
    }
}