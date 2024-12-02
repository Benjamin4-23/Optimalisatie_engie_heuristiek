package org.example.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.example.domain.Edge;
import org.example.domain.Graph;
import org.example.search.MySolution;
import org.example.search.framework.Solution;

public class OutputWriter {
    private Graph graph;
    private DataReader reader;
    private StringBuilder jsonBuffer;

    public OutputWriter(MySolution solution) {
        // dijkstra
        this.graph = solution.getGraph();
        this.reader = solution.reader;
        jsonBuffer = new StringBuilder();
    }

    public void write(String fileName, double objectiveValue) {
        StringBuilder jsonBuffer = new StringBuilder();

        jsonBuffer.append(String.format("{\n\t\"objective_value\":%.4f", objectiveValue).replace(",", ".") + ",");
        jsonBuffer.append("\n\t\"edges\": [");

        // Write used edges to buffer
        for (Edge edge : this.graph.edges.values()) {
            if (edge.isUsed && edge.endNode1.x != 0 && edge.endNode1.y != 0 && edge.endNode2.x != 0 && edge.endNode2.y != 0) {
                if(!edge.oldEdges.isEmpty()){
                    for (Edge e : edge.oldEdges) {
                        jsonBuffer.append(String.format("%d,", e.originalID));
                    }
                } else {
                    jsonBuffer.append(String.format("%d,", edge.originalID));
                }
            }
        }
        if (!this.graph.edges.isEmpty()) {
            jsonBuffer.setLength(jsonBuffer.length() - 1); // Remove last comma
        }
        jsonBuffer.append("],\n\t\"nodes\": [");
        // Write used edges to buffer
        for (Edge edge : this.graph.edges.values()) {
            if (edge.isUsed && edge.endNode1.x != 0 && edge.endNode1.y != 0 && edge.endNode2.x != 0 && edge.endNode2.y != 0) {
                jsonBuffer.append(String.format("[%d,%d],", edge.endNode1.id, edge.endNode2.id));
            }
        }
        if (!this.graph.edges.isEmpty()) {
            jsonBuffer.setLength(jsonBuffer.length() - 1); // Remove last comma
        }
        jsonBuffer.append("]\n}");

        // Write buffer to JSON file
        try {
            Files.write(Paths.get(fileName), jsonBuffer.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addField(){

    }
}

