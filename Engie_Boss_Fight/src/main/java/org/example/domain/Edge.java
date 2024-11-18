package org.example.domain;

public class Edge {
    public int id;
    public EdgeType edgeType;
    public int cost;
    public boolean isUsed;

    public Node endNode1;
    public Node endNode2;

    public enum EdgeType {
        REGULAR, OFFSTREET, EXISTING
    }

    public Edge(Edge edge) {
        this.id = edge.id;
        this.edgeType = edge.edgeType;
        this.cost = edge.cost;
        this.endNode1 = edge.endNode1;
        this.endNode2 = edge.endNode2;
        this.isUsed = edge.isUsed;
    }

    public Edge(int id, EdgeType edgeType, int cost, Node endpoint1, Node endpoint2) {
        this.id = id;
        this.edgeType = edgeType;
        this.cost = cost;
        this.endNode1 = endpoint1;
        this.endNode2 = endpoint2;
        this.isUsed = false;
    }

    public void Use(){
        this.isUsed = true;
    }

    @Override
    public String toString() {
        return String.format("Edge_%d_%d", endNode1.id, endNode2.id);
    }

    // Getters and setters can be added here if needed
}