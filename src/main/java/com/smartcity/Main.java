package com.smartcity;

import com.smartcity.common.*;
import com.smartcity.graph.scc.TarjanSCC;
import com.smartcity.graph.topo.TopologicalSort;
import com.smartcity.graph.dagsp.DAGShortestPath;

import java.io.IOException;
import java.util.List;

/**
 * Main application entry point for Smart City Scheduling.
 * Demonstrates SCC detection, topological sorting, and DAG shortest paths.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar smart-city-scheduling.jar <path-to-graph.json>");
            System.out.println("Example: java -jar smart-city-scheduling.jar data/tasks.json");
            return;
        }

        String filename = args[0];

        try {
            System.out.println("=".repeat(60));
            System.out.println("Smart City Scheduling - Graph Analysis");
            System.out.println("=".repeat(60));
            System.out.println();

            // Load graph
            System.out.println("Loading graph from: " + filename);
            GraphLoader.GraphData data = GraphLoader.loadFromFile(filename);
            Graph graph = data.graph;
            int source = data.source;

            System.out.println(graph);
            System.out.println("Source vertex: " + source);
            System.out.println("Weight model: " + data.weightModel);
            System.out.println();

            // 1. Find Strongly Connected Components
            System.out.println("=".repeat(60));
            System.out.println("1. STRONGLY CONNECTED COMPONENTS (Tarjan's Algorithm)");
            System.out.println("=".repeat(60));

            TarjanSCC sccFinder = new TarjanSCC(graph);
            List<List<Integer>> sccs = sccFinder.findSCCs();

            System.out.println("Found " + sccs.size() + " strongly connected components:");
            for (int i = 0; i < sccs.size(); i++) {
                List<Integer> scc = sccs.get(i);
                System.out.printf("  SCC %d (size %d): %s\n", i, scc.size(), scc);
            }
            System.out.println();

            sccFinder.getMetrics().printSummary();
            System.out.println();

            // Build condensation graph
            System.out.println("Building condensation graph (DAG of SCCs)...");
            Graph condensation = sccFinder.buildCondensationGraph();
            System.out.println(condensation);

            // 2. Topological Sort
            System.out.println("=".repeat(60));
            System.out.println("2. TOPOLOGICAL SORT (Kahn's Algorithm)");
            System.out.println("=".repeat(60));

            TopologicalSort topoSort = new TopologicalSort(condensation);
            List<Integer> topoOrder = topoSort.sort();

            if (topoOrder != null) {
                System.out.println("Topological order of SCCs: " + topoOrder);

                // Expand to original vertices
                List<Integer> expandedOrder = TopologicalSort.expandSCCOrder(topoOrder, sccs);
                System.out.println("Expanded order (original vertices): " + expandedOrder);
                System.out.println();

                topoSort.getMetrics().printSummary();
            } else {
                System.out.println("ERROR: Cycle detected in condensation graph!");
            }
            System.out.println();

            // 3. Shortest Paths in DAG
            if (topoOrder != null) {
                System.out.println("=".repeat(60));
                System.out.println("3. SHORTEST PATHS IN DAG");
                System.out.println("=".repeat(60));

                // Map source to its SCC
                int[] vertexToSCC = sccFinder.getVertexToSCCMapping();
                int sccSource = vertexToSCC[source];

                DAGShortestPath dagSP = new DAGShortestPath(condensation);

                // Shortest paths
                System.out.println("Computing shortest paths from SCC " + sccSource + "...");
                DAGShortestPath.PathResult shortestResult = dagSP.shortestPaths(sccSource);

                System.out.println("Shortest distances from SCC " + sccSource + ":");
                for (int i = 0; i < shortestResult.dist.length; i++) {
                    if (shortestResult.dist[i] != Integer.MAX_VALUE) {
                        System.out.printf("  To SCC %d: %d\n", i, shortestResult.dist[i]);
                        List<Integer> path = shortestResult.reconstructPath(sccSource, i);
                        System.out.printf("    Path: %s\n", path);
                    }
                }
                System.out.println();

                dagSP.getMetrics().printSummary();
                System.out.println();

                // Longest paths (Critical Path)
                System.out.println("=".repeat(60));
                System.out.println("4. CRITICAL PATH (Longest Path)");
                System.out.println("=".repeat(60));

                DAGShortestPath dagLP = new DAGShortestPath(condensation);
                DAGShortestPath.CriticalPathResult criticalPath =
                        dagLP.findCriticalPath(sccSource);

                System.out.println(criticalPath);
                System.out.println();

                dagLP.getMetrics().printSummary();
            }

            System.out.println();
            System.out.println("=".repeat(60));
            System.out.println("Analysis complete!");
            System.out.println("=".repeat(60));

        } catch (IOException e) {
            System.err.println("Error loading graph file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error during analysis: " + e.getMessage());
            e.printStackTrace();
        }
    }
}