package org.example.domain;

import java.util.*;
import java.util.HashMap;

public class Node {
    public int id;
    public double x;
    public double y;
    public NodeType nodeType;

    public HashMap<Integer, Edge> edges = new HashMap<>();


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
        this.edges = new HashMap<>(node.edges);
    }

    public void removeEdgeWithNode(Node node) {
        Edge delete = null;
        for (Edge edge : this.edges.values()){
            if(edge.endNode1.id == node.id || edge.endNode2.id == node.id){
                delete = edge;
                break;
            }
        }
        if(delete != null){
            this.edges.remove(delete.id);
        }
    }

    @Override
    public String toString() {
        return "id: " + this.id + ", x: " + this.x + ", y: " + this.y + ", nodeType: " + this.nodeType + ", edges: " + this.edges.size();
    }
// Getters and setters can be added here if needed
}