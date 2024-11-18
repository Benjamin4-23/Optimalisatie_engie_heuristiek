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

        jsonBuffer.append("{\n  \"edges\": [\n");

        // Write used edges to buffer
        for (Edge edge : this.graph.edges.values()) {
            if (edge.isUsed && edge.endNode1.x != 0 && edge.endNode1.y != 0 && edge.endNode2.x != 0 && edge.endNode2.y != 0) {
                String edge1 = "\""+edge.endNode1.x+","+edge.endNode1.y+"\"";
                String edge2 = "\""+edge.endNode2.x+","+edge.endNode2.y+"\"";
                jsonBuffer.append("    { \"startNode\": ").append(edge1)
                        .append(", \"endNode\": ").append(edge2).append(" },\n");
            }
        }
        if (!this.graph.edges.isEmpty()) {
            jsonBuffer.setLength(jsonBuffer.length() - 2); // Remove last comma
        }
        jsonBuffer.append("\n  ]\n}");

        // Write buffer to JSON file
        try {
            Files.write(Paths.get(fileName), jsonBuffer.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
