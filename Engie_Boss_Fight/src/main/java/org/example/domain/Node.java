package org.example.domain;

import java.util.*;
import java.util.HashMap;

public class Node {
    public int id;
    public double x;
    public double y;
    public NodeType nodeType;

    public HashMap<Integer, Edge> outgoingEdges = new HashMap<>();
    public HashMap<Integer, Edge> incomingEdges = new HashMap<>();


    public Node(int id, double x, double y, NodeType nodeType) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.nodeType = nodeType;
    }

    public Node(Node node) {
        this.id = node.id;
        this.x = node.x;
        this.y = node.y;
        this.nodeType = node.nodeType;
        this.outgoingEdges = new HashMap<>(node.outgoingEdges);
        this.incomingEdges = new HashMap<>(node.incomingEdges);
    }

    public void removeEdgesWithNode(Node node) {
        List<Integer> outgoingToRemove = new ArrayList<>();
        for (Edge edge : outgoingEdges.values()) {
            if (edge.endNode2 == node) {
                outgoingToRemove.add(edge.id);
            }
        }
        for (int id : outgoingToRemove) {
            outgoingEdges.remove(id);
        }

        List<Integer> incomingToRemove = new ArrayList<>();
        for (Edge edge : incomingEdges.values()) {
            if (edge.endNode1 == node) {
                incomingToRemove.add(edge.id);
            }
        }
        for (int id : incomingToRemove) {
            incomingEdges.remove(id);
        }
    }

    public boolean isConnected() {
        if (this.nodeType == NodeType.PROSPECT) {
            // check if there are incoming edges that are active
            for (Edge edge : incomingEdges.values()) {
                {
                    if (edge.isUsed) {
                        return true;
                    }
                }
            }
        } else {
            for (Edge edge : incomingEdges.values()) {
                if (edge.isUsed) {
                    return true;
                }
            }
            for (Edge edge : outgoingEdges.values()) {
                if (edge.isUsed) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "id: " + id + ", x: " + x + ", y: " + y + ", nodeType: " + nodeType + ", outgoingEdges: " + outgoingEdges.size() + ", incomingEdges: " + incomingEdges.size();
    }
// Getters and setters can be added here if needed
}