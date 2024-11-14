package org.example.domain;

import java.util.HashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {
    public int id;
    public double x;
    public double y;
    public NodeType nodeType;

    public HashMap<Integer, Edge> outgoingEdges = new HashMap<>();
    public HashMap<Integer, Edge> incomingEdges = new HashMap<>();

    public Node() {

    }
    public enum NodeType {
        REGULAR, PROSPECT
    }

    public Node(int id, double x, double y, NodeType nodeType) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.nodeType = nodeType;
    }

    // Getters and setters can be added here if needed
}