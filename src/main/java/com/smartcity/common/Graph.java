package com.smartcity.common;

import java.util.*;

/**
 * Directed weighted graph representation using adjacency lists.
 */
public class Graph {
    private final int n;
    private final List<List<Edge>> adj;
    private final boolean directed;

    /**
     * Edge representation with destination and weight
     */
    public static class Edge {
        public final int to;
        public final int weight;

        public Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return String.format("->%d(w=%d)", to, weight);
        }
    }

    /**
     * Create a graph with n vertices
     * @param n number of vertices
     * @param directed true if directed graph
     */
    public Graph(int n, boolean directed) {
        this.n = n;
        this.directed = directed;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }

    /**
     * Add an edge from u to v with weight w
     */
    public void addEdge(int u, int v, int weight) {
        adj.get(u).add(new Edge(v, weight));
        if (!directed) {
            adj.get(v).add(new Edge(u, weight));
        }
    }

    /**
     * Get all neighbors of vertex u
     */
    public List<Edge> getNeighbors(int u) {
        return adj.get(u);
    }

    /**
     * Get number of vertices
     */
    public int getN() {
        return n;
    }

    /**
     * Check if graph is directed
     */
    public boolean isDirected() {
        return directed;
    }

    /**
     * Get the reverse graph (transpose)
     */
    public Graph getReverse() {
        if (!directed) {
            throw new UnsupportedOperationException("Cannot reverse undirected graph");
        }

        Graph rev = new Graph(n, true);
        for (int u = 0; u < n; u++) {
            for (Edge e : adj.get(u)) {
                rev.addEdge(e.to, u, e.weight);
            }
        }
        return rev;
    }

    /**
     * Get total number of edges
     */
    public int getEdgeCount() {
        int count = 0;
        for (int i = 0; i < n; i++) {
            count += adj.get(i).size();
        }
        return directed ? count : count / 2;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Graph(n=%d, edges=%d, %s)\n",
                n, getEdgeCount(), directed ? "directed" : "undirected"));
        for (int u = 0; u < n; u++) {
            if (!adj.get(u).isEmpty()) {
                sb.append(String.format("  %d: %s\n", u, adj.get(u)));
            }
        }
        return sb.toString();
    }
}