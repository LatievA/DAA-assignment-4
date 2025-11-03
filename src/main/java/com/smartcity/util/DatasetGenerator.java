package com.smartcity.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Utility to generate test datasets for graph algorithms.
 * Creates graphs of various sizes and structures.
 */
public class DatasetGenerator {

    private final Random random;

    public DatasetGenerator(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Generate a dataset with specified parameters
     * @param n number of vertices
     * @param density edge density (0.0 to 1.0)
     * @param hasCycles whether to include cycles
     * @param filename output filename
     */
    public void generateDataset(int n, double density, boolean hasCycles, String filename) {
        JsonObject json = new JsonObject();
        json.addProperty("directed", true);
        json.addProperty("n", n);

        JsonArray edges = new JsonArray();

        if (hasCycles) {
            // Create cycles and additional edges
            edges = generateGraphWithCycles(n, density);
        } else {
            // Create DAG
            edges = generateDAG(n, density);
        }

        json.add("edges", edges);
        json.addProperty("source", 0);
        json.addProperty("weight_model", "edge");

        // Write to file
        try (FileWriter writer = new FileWriter(filename)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(json, writer);
            System.out.println("Generated: " + filename);
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }

    /**
     * Generate DAG with topological structure
     */
    private JsonArray generateDAG(int n, double density) {
        JsonArray edges = new JsonArray();
        int maxEdges = n * (n - 1) / 2;
        int targetEdges = (int) (maxEdges * density);

        // Create edges only from lower to higher indices (ensures DAG)
        for (int u = 0; u < n - 1; u++) {
            for (int v = u + 1; v < n; v++) {
                if (edges.size() < targetEdges && random.nextDouble() < density) {
                    JsonObject edge = new JsonObject();
                    edge.addProperty("u", u);
                    edge.addProperty("v", v);
                    edge.addProperty("w", random.nextInt(10) + 1);
                    edges.add(edge);
                }
            }
        }

        // Ensure connectivity: create a path from 0 to n-1
        if (edges.size() == 0 || !isConnectedPath(edges, n)) {
            for (int i = 0; i < n - 1; i++) {
                if (!hasEdge(edges, i, i + 1)) {
                    JsonObject edge = new JsonObject();
                    edge.addProperty("u", i);
                    edge.addProperty("v", i + 1);
                    edge.addProperty("w", random.nextInt(5) + 1);
                    edges.add(edge);
                }
            }
        }

        return edges;
    }

    /**
     * Generate graph with cycles
     */
    private JsonArray generateGraphWithCycles(int n, double density) {
        JsonArray edges = new JsonArray();
        int maxEdges = n * (n - 1);
        int targetEdges = (int) (maxEdges * density);

        // Create some cycles
        int numCycles = Math.max(1, n / 4);
        for (int i = 0; i < numCycles; i++) {
            int cycleSize = random.nextInt(3) + 2; // 2-4 vertices per cycle
            int start = random.nextInt(Math.max(1, n - cycleSize));

            for (int j = 0; j < cycleSize; j++) {
                int u = start + j;
                int v = start + ((j + 1) % cycleSize);

                JsonObject edge = new JsonObject();
                edge.addProperty("u", u);
                edge.addProperty("v", v);
                edge.addProperty("w", random.nextInt(10) + 1);
                edges.add(edge);
            }
        }

        // Add random edges to reach target density
        while (edges.size() < targetEdges) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);

            if (u != v && !hasEdge(edges, u, v)) {
                JsonObject edge = new JsonObject();
                edge.addProperty("u", u);
                edge.addProperty("v", v);
                edge.addProperty("w", random.nextInt(10) + 1);
                edges.add(edge);
            }
        }

        return edges;
    }

    /**
     * Check if edge exists
     */
    private boolean hasEdge(JsonArray edges, int u, int v) {
        for (int i = 0; i < edges.size(); i++) {
            JsonObject edge = edges.get(i).getAsJsonObject();
            if (edge.get("u").getAsInt() == u && edge.get("v").getAsInt() == v) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if there's a connected path
     */
    private boolean isConnectedPath(JsonArray edges, int n) {
        boolean[] reachable = new boolean[n];
        reachable[0] = true;

        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < edges.size(); i++) {
                JsonObject edge = edges.get(i).getAsJsonObject();
                int u = edge.get("u").getAsInt();
                int v = edge.get("v").getAsInt();

                if (reachable[u] && !reachable[v]) {
                    reachable[v] = true;
                    changed = true;
                }
            }
        }

        return reachable[n - 1];
    }

    /**
     * Main method to generate all datasets
     */
    public static void main(String[] args) {
        DatasetGenerator gen = new DatasetGenerator(42);

        System.out.println("Generating datasets...");
        System.out.println();

        // Small datasets (6-10 nodes)
        gen.generateDataset(6, 0.3, false, "data/small_dag_1.json");
        gen.generateDataset(8, 0.4, true, "data/small_cycle_1.json");
        gen.generateDataset(10, 0.25, false, "data/small_dag_2.json");

        // Medium datasets (10-20 nodes)
        gen.generateDataset(12, 0.3, true, "data/medium_mixed_1.json");
        gen.generateDataset(15, 0.35, true, "data/medium_mixed_2.json");
        gen.generateDataset(18, 0.2, false, "data/medium_dag_1.json");

        // Large datasets (20-50 nodes)
        gen.generateDataset(25, 0.15, true, "data/large_sparse_1.json");
        gen.generateDataset(35, 0.25, true, "data/large_mixed_1.json");
        gen.generateDataset(50, 0.1, false, "data/large_dag_1.json");

        System.out.println();
        System.out.println("All datasets generated successfully!");
    }
}