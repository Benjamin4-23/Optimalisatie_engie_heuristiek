package org.example.domain;

public class Edge {
    public int id;
    public EdgeType edgeType;
    public int cost;

    public Node endNode1;
    public Node endNode2;

    public Edge() {

    }

    public enum EdgeType {
        REGULAR, OFFSTREET, EXISTING
    }

    public Edge(int id, EdgeType edgeType, int cost, Node endpoint1, Node endpoint2) {
        this.id = id;
        this.edgeType = edgeType;
        this.cost = cost;
        this.endNode1 = endpoint1;
        this.endNode2 = endpoint2;
    }

    @Override
    public String toString() {
        return String.format("Edge_%d_%d", endNode1.id, endNode2.id);
    }

    // Getters and setters can be added here if needed
}