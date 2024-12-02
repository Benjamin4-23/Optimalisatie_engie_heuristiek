package org.example.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.example.domain.Edge;
import org.example.domain.Graph;
import org.example.domain.Node;
import org.example.search.MySolution;
import org.example.search.framework.Solution;

public class OutputWriter {
    private Graph graph;
    private DataReader reader;
    private StringBuilder jsonBuffer;
    private double objectiveValue;

    public OutputWriter(MySolution solution) {
        // dijkstra
        this.graph = solution.getGraph();
        this.reader = solution.reader;
        this.objectiveValue = solution.getObjectiveValue();
        jsonBuffer = new StringBuilder();
    }

    public void write(String fileName) {
        //StringBuilder jsonBuffer = new StringBuilder();
        jsonBuffer.append(String.format("{\"objective_value\":%.4f", objectiveValue).replace(",", ".") + ",");
        jsonBuffer.append("\n\"edges\": [");

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
        jsonBuffer.append("],");

        addField("simplify", reader.simplifiedNodes.values(), reader.simplifiedEdges.values());
        addField("shave", reader.shavedNodes.values(), reader.shavedEdges.values());

        jsonBuffer.setLength(jsonBuffer.length() - 1);
        jsonBuffer.append("}");

        // Write buffer to JSON file
        try {
            Files.write(Paths.get(fileName), jsonBuffer.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addField(String name, Collection<Node> nodes, Collection<Edge> edges){
        jsonBuffer.append(String.format("\n\"%s\":{", name));
        jsonBuffer.append("\"nodes\":[");
        for (Node n: nodes) {
            if(n.id == -1) continue;
            jsonBuffer.append(String.format("%d,",n.id));
        }
        jsonBuffer.setLength(jsonBuffer.length() - 1);
        jsonBuffer.append("],");
        jsonBuffer.append("\"edges\":[");
        for (Edge e: edges) {
            if(e.endNode2.id == -1 || e.endNode1.id == -1) continue;
            jsonBuffer.append(String.format("%d,", e.originalID));
        }
        jsonBuffer.setLength(jsonBuffer.length() - 1);
        jsonBuffer.append("],");
        jsonBuffer.append("\"direct\":[");
        for (Edge e: edges) {
            if(e.endNode2.id == -1 || e.endNode1.id == -1) continue;
            jsonBuffer.append(String.format("[%d,%d],", e.endNode1.id, e.endNode2.id));
        }
        jsonBuffer.setLength(jsonBuffer.length() - 1);
        jsonBuffer.append("]},");
    }
}

