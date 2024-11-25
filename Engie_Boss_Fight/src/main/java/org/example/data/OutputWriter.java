package org.example.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.example.domain.Edge;
import org.example.domain.Graph;

public class OutputWriter {
    private Graph graph;
    public OutputWriter(Graph graph) {
        this.graph = graph;
    }

    public void write(String fileName) {
        // Prepare JSON buffer
        StringBuilder jsonBuffer = new StringBuilder();

        jsonBuffer.append(String.format("{\"objective_value\":%d,", 123456));
        jsonBuffer.append("\"edges\": [");

        // Write used edges to buffer
        for (Edge edge : this.graph.edges.values()) {
            if (edge.isUsed && edge.endNode1.x != 0 && edge.endNode1.y != 0 && edge.endNode2.x != 0 && edge.endNode2.y != 0) {
                jsonBuffer.append(String.format("%d,", edge.id));
            }
        }
        if (!this.graph.edges.isEmpty()) {
            jsonBuffer.setLength(jsonBuffer.length() - 2); // Remove last comma
        }
        jsonBuffer.append("]}");

        // Write buffer to JSON file
        try {
            Files.write(Paths.get("output/"+fileName), jsonBuffer.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
