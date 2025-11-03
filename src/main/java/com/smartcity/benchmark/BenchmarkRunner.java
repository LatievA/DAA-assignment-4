package com.smartcity.benchmark;

import com.smartcity.common.Graph;
import com.smartcity.common.GraphLoader;
import com.smartcity.common.Metrics;
import com.smartcity.graph.dagsp.DAGShortestPath;
import com.smartcity.graph.scc.TarjanSCC;
import com.smartcity.graph.topo.TopologicalSort;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Benchmark runner that executes all datasets and outputs results in CSV format.
 * Measures performance metrics for SCC, Topological Sort, and DAG Shortest Path algorithms.
 */
public class BenchmarkRunner {

    /**
     * Result of running all algorithms on a single dataset
     */
    public static class BenchmarkResult {
        public String dataset;
        public int nodes;
        public int edges;
        public double density;
        public boolean hasCycles;

        // SCC metrics
        public long sccTimeNanos;
        public long sccDfsVisits;
        public long sccEdgesExplored;
        public long sccStackPops;
        public int numSCCs;

        // Topological Sort metrics
        public long topoTimeNanos;
        public long topoQueuePushes;
        public long topoQueuePops;
        public long topoEdgesProcessed;
        public boolean topoSuccess;

        // DAG Shortest Path metrics (on condensation graph)
        public long dagspTimeNanos;
        public long dagspRelaxations;
        public long dagspUpdates;
        public int criticalPathLength;

        public BenchmarkResult(String dataset) {
            this.dataset = dataset;
        }

        /**
         * Convert result to CSV row
         */
        public String toCSV() {
            return String.format("%s,%d,%d,%.4f,%b,%d,%d,%d,%d,%d,%d,%d,%d,%d,%b,%d,%d,%d,%d",
                    dataset,
                    nodes,
                    edges,
                    density,
                    hasCycles,
                    sccTimeNanos,
                    sccDfsVisits,
                    sccEdgesExplored,
                    sccStackPops,
                    numSCCs,
                    topoTimeNanos,
                    topoQueuePushes,
                    topoQueuePops,
                    topoEdgesProcessed,
                    topoSuccess,
                    dagspTimeNanos,
                    dagspRelaxations,
                    dagspUpdates,
                    criticalPathLength
            );
        }

        /**
         * CSV header
         */
        public static String getCSVHeader() {
            return "Dataset,Nodes,Edges,Density,HasCycles," +
                    "SCC_Time_Nanos,SCC_DFS_Visits,SCC_Edges_Explored,SCC_Stack_Pops,Num_SCCs," +
                    "Topo_Time_Nanos,Topo_Queue_Pushes,Topo_Queue_Pops,Topo_Edges_Processed,Topo_Success," +
                    "DAGSP_Time_Nanos,DAGSP_Relaxations,DAGSP_Updates,Critical_Path_Length";
        }
    }

    /**
     * Run benchmark on a single dataset
     */
    public BenchmarkResult runBenchmark(String filename) {
        BenchmarkResult result = new BenchmarkResult(new File(filename).getName());

        try {
            // Load graph
            GraphLoader.GraphData data = GraphLoader.loadFromFile(filename);
            Graph graph = data.graph;
            int source = data.source;

            result.nodes = graph.getN();
            result.edges = graph.getEdgeCount();
            result.density = calculateDensity(result.nodes, result.edges);

            // 1. Run SCC detection
            TarjanSCC sccFinder = new TarjanSCC(graph);
            List<List<Integer>> sccs = sccFinder.findSCCs();
            Metrics sccMetrics = sccFinder.getMetrics();

            result.numSCCs = sccs.size();
            result.sccTimeNanos = sccMetrics.getElapsedNanos();
            result.sccDfsVisits = sccMetrics.getCount("dfs_visits");
            result.sccEdgesExplored = sccMetrics.getCount("edges_explored");
            result.sccStackPops = sccMetrics.getCount("stack_pops");
            result.hasCycles = (result.numSCCs < result.nodes);

            // 2. Build condensation graph
            Graph condensation = sccFinder.buildCondensationGraph();

            // 3. Run Topological Sort on condensation
            TopologicalSort topoSort = new TopologicalSort(condensation);
            List<Integer> topoOrder = topoSort.sort();
            Metrics topoMetrics = topoSort.getMetrics();

            result.topoSuccess = (topoOrder != null);
            result.topoTimeNanos = topoMetrics.getElapsedNanos();
            result.topoQueuePushes = topoMetrics.getCount("queue_pushes");
            result.topoQueuePops = topoMetrics.getCount("queue_pops");
            result.topoEdgesProcessed = topoMetrics.getCount("edges_processed");

            // 4. Run DAG Shortest Path (if topological sort succeeded)
            if (topoOrder != null && condensation.getN() > 0) {
                // Map source to its SCC
                int[] vertexToSCC = sccFinder.getVertexToSCCMapping();
                int sccSource = vertexToSCC[source];

                DAGShortestPath dagsp = new DAGShortestPath(condensation);
                DAGShortestPath.CriticalPathResult criticalPath = dagsp.findCriticalPath(sccSource);
                Metrics dagspMetrics = dagsp.getMetrics();

                result.dagspTimeNanos = dagspMetrics.getElapsedNanos();
                result.dagspRelaxations = dagspMetrics.getCount("relaxations");
                result.dagspUpdates = dagspMetrics.getCount("updates");
                result.criticalPathLength = criticalPath.length;
            }

        } catch (Exception e) {
            System.err.println("Error processing " + filename + ": " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Calculate graph density
     */
    private double calculateDensity(int nodes, int edges) {
        if (nodes <= 1) return 0.0;
        int maxEdges = nodes * (nodes - 1); // For directed graph
        return (double) edges / maxEdges;
    }

    /**
     * Run benchmarks on all datasets in a directory
     */
    public List<BenchmarkResult> runAllBenchmarks(String dataDirectory) {
        List<BenchmarkResult> results = new ArrayList<>();

        File dir = new File(dataDirectory);
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("Directory not found: " + dataDirectory);
            return results;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            System.err.println("No JSON files found in: " + dataDirectory);
            return results;
        }

        System.out.println("Running benchmarks on " + files.length + " datasets...");
        System.out.println();

        for (File file : files) {
            System.out.println("Processing: " + file.getName());
            BenchmarkResult result = runBenchmark(file.getAbsolutePath());
            results.add(result);

            // Print summary
            System.out.printf("  Nodes: %d, Edges: %d, Density: %.4f%n",
                    result.nodes, result.edges, result.density);
            System.out.printf("  SCCs: %d, Has Cycles: %b%n",
                    result.numSCCs, result.hasCycles);
            System.out.printf("  Times (ns): SCC=%d, Topo=%d, DAGSP=%d%n",
                    result.sccTimeNanos, result.topoTimeNanos, result.dagspTimeNanos);
            System.out.println();
        }

        return results;
    }

    /**
     * Write results to CSV file
     */
    public void writeResultsToCSV(List<BenchmarkResult> results, String outputFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            // Write header
            writer.println(BenchmarkResult.getCSVHeader());

            // Write data rows
            for (BenchmarkResult result : results) {
                writer.println(result.toCSV());
            }

            System.out.println("Results written to: " + outputFile);

        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Print summary statistics
     */
    public void printSummaryStatistics(List<BenchmarkResult> results) {
        if (results.isEmpty()) {
            System.out.println("No results to summarize.");
            return;
        }

        System.out.println("=".repeat(80));
        System.out.println("BENCHMARK SUMMARY STATISTICS");
        System.out.println("=".repeat(80));
        System.out.println();

        // Group by size
        List<BenchmarkResult> small = new ArrayList<>();
        List<BenchmarkResult> medium = new ArrayList<>();
        List<BenchmarkResult> large = new ArrayList<>();

        for (BenchmarkResult r : results) {
            if (r.nodes <= 10) small.add(r);
            else if (r.nodes <= 20) medium.add(r);
            else large.add(r);
        }

        System.out.println("Dataset Distribution:");
        System.out.printf("  Small (≤10 nodes): %d datasets%n", small.size());
        System.out.printf("  Medium (11-20 nodes): %d datasets%n", medium.size());
        System.out.printf("  Large (>20 nodes): %d datasets%n", large.size());
        System.out.println();

        // Average times by category
        if (!small.isEmpty()) {
            printCategoryStats("Small", small);
        }
        if (!medium.isEmpty()) {
            printCategoryStats("Medium", medium);
        }
        if (!large.isEmpty()) {
            printCategoryStats("Large", large);
        }

        // Overall statistics
        System.out.println("Overall Averages:");
        long avgSccTime = (long) results.stream().mapToLong(r -> r.sccTimeNanos).average().orElse(0);
        long avgTopoTime = (long) results.stream().mapToLong(r -> r.topoTimeNanos).average().orElse(0);
        long avgDagspTime = (long) results.stream().mapToLong(r -> r.dagspTimeNanos).average().orElse(0);

        System.out.printf("  SCC Time: %,d ns (%.3f ms)%n", avgSccTime, avgSccTime / 1_000_000.0);
        System.out.printf("  Topo Time: %,d ns (%.3f ms)%n", avgTopoTime, avgTopoTime / 1_000_000.0);
        System.out.printf("  DAGSP Time: %,d ns (%.3f ms)%n", avgDagspTime, avgDagspTime / 1_000_000.0);
        System.out.println();

        // Graphs with cycles
        long cycleCount = results.stream().filter(r -> r.hasCycles).count();
        System.out.printf("Graphs with cycles: %d / %d (%.1f%%)%n",
                cycleCount, results.size(), 100.0 * cycleCount / results.size());
        System.out.println();
    }

    /**
     * Print statistics for a category
     */
    private void printCategoryStats(String category, List<BenchmarkResult> results) {
        System.out.println(category + " Datasets:");

        double avgNodes = results.stream().mapToInt(r -> r.nodes).average().orElse(0);
        double avgEdges = results.stream().mapToInt(r -> r.edges).average().orElse(0);
        double avgDensity = results.stream().mapToDouble(r -> r.density).average().orElse(0);

        long avgSccTime = (long) results.stream().mapToLong(r -> r.sccTimeNanos).average().orElse(0);
        long avgTopoTime = (long) results.stream().mapToLong(r -> r.topoTimeNanos).average().orElse(0);
        long avgDagspTime = (long) results.stream().mapToLong(r -> r.dagspTimeNanos).average().orElse(0);

        System.out.printf("  Avg Nodes: %.1f, Avg Edges: %.1f, Avg Density: %.4f%n",
                avgNodes, avgEdges, avgDensity);
        System.out.printf("  Avg SCC Time: %,d ns (%.3f ms)%n",
                avgSccTime, avgSccTime / 1_000_000.0);
        System.out.printf("  Avg Topo Time: %,d ns (%.3f ms)%n",
                avgTopoTime, avgTopoTime / 1_000_000.0);
        System.out.printf("  Avg DAGSP Time: %,d ns (%.3f ms)%n",
                avgDagspTime, avgDagspTime / 1_000_000.0);
        System.out.println();
    }

    /**
     * Main method to run benchmarks
     */
    public static void main(String[] args) {
        String dataDirectory = "data";
        String outputFile = "benchmark_results.csv";

        // Parse command line arguments
        if (args.length >= 1) {
            dataDirectory = args[0];
        }
        if (args.length >= 2) {
            outputFile = args[1];
        }

        System.out.println("=".repeat(80));
        System.out.println("SMART CITY SCHEDULING - BENCHMARK RUNNER");
        System.out.println("=".repeat(80));
        System.out.println();
        System.out.println("Data Directory: " + dataDirectory);
        System.out.println("Output File: " + outputFile);
        System.out.println();

        BenchmarkRunner runner = new BenchmarkRunner();

        // Run all benchmarks several times to minimize JVM effect on time
        List<BenchmarkResult> results = runner.runAllBenchmarks(dataDirectory);
        results = runner.runAllBenchmarks(dataDirectory);
        results = runner.runAllBenchmarks(dataDirectory);
        results = runner.runAllBenchmarks(dataDirectory);
        results = runner.runAllBenchmarks(dataDirectory);
        results = runner.runAllBenchmarks(dataDirectory);
        results = runner.runAllBenchmarks(dataDirectory);
        results = runner.runAllBenchmarks(dataDirectory);
        results = runner.runAllBenchmarks(dataDirectory);


        if (results.isEmpty()) {
            System.err.println("No results generated. Exiting.");
            return;
        }

        // Write to CSV
        runner.writeResultsToCSV(results, outputFile);
        System.out.println();

        // Print summary statistics
        runner.printSummaryStatistics(results);

        System.out.println("=".repeat(80));
        System.out.println("BENCHMARK COMPLETE");
        System.out.println("=".repeat(80));
    }
}