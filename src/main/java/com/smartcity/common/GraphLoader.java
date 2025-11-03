package com.smartcity.common;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.FileReader;
import java.io.IOException;

/**
 * Utility class to load graph data from JSON files.
 */
public class GraphLoader {

    /**
     * Graph data structure from JSON
     */
    public static class GraphData {
        public Graph graph;
        public int source;
        public String weightModel;

        public GraphData(Graph graph, int source, String weightModel) {
            this.graph = graph;
            this.source = source;
            this.weightModel = weightModel;
        }
    }

    /**
     * Load graph from JSON file
     * @param filename path to JSON file
     * @return GraphData object containing the graph and metadata
     * @throws IOException if file cannot be read
     */
    public static GraphData loadFromFile(String filename) throws IOException {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(new FileReader(filename), JsonObject.class);

        boolean directed = json.get("directed").getAsBoolean();
        int n = json.get("n").getAsInt();

        Graph graph = new Graph(n, directed);

        JsonArray edges = json.getAsJsonArray("edges");
        for (JsonElement edgeElement : edges) {
            JsonObject edge = edgeElement.getAsJsonObject();
            int u = edge.get("u").getAsInt();
            int v = edge.get("v").getAsInt();
            int w = edge.has("w") ? edge.get("w").getAsInt() : 1;

            graph.addEdge(u, v, w);
        }

        int source = json.has("source") ? json.get("source").getAsInt() : 0;
        String weightModel = json.has("weight_model") ?
                json.get("weight_model").getAsString() : "edge";

        return new GraphData(graph, source, weightModel);
    }

    /**
     * Load graph from JSON string
     * @param jsonString JSON string
     * @return GraphData object containing the graph and metadata
     */
    public static GraphData loadFromString(String jsonString) {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(jsonString, JsonObject.class);

        boolean directed = json.get("directed").getAsBoolean();
        int n = json.get("n").getAsInt();

        Graph graph = new Graph(n, directed);

        JsonArray edges = json.getAsJsonArray("edges");
        for (JsonElement edgeElement : edges) {
            JsonObject edge = edgeElement.getAsJsonObject();
            int u = edge.get("u").getAsInt();
            int v = edge.get("v").getAsInt();
            int w = edge.has("w") ? edge.get("w").getAsInt() : 1;

            graph.addEdge(u, v, w);
        }

        int source = json.has("source") ? json.get("source").getAsInt() : 0;
        String weightModel = json.has("weight_model") ?
                json.get("weight_model").getAsString() : "edge";

        return new GraphData(graph, source, weightModel);
    }
}