package org.example.domain;

import java.util.ArrayList;
import java.util.List;

public class Edge {
    public int id;
    public EdgeType edgeType;
    public int cost;
    public final int originalCost;
    public boolean isUsed;

    public Node endNode1;
    public Node endNode2;
    public int originalID;
    public List<Edge> oldEdges;
    public boolean isReplaceable;
    public int replaceableCost;
    List<Edge> replacingEdges;
    public boolean isMoved;

    public Edge(Edge edge) {
        this.id = edge.id;
        this.edgeType = edge.edgeType;
        this.cost = edge.cost;
        this.originalCost = edge.originalCost;
        this.endNode1 = edge.endNode1;
        this.endNode2 = edge.endNode2;
        this.isUsed = edge.isUsed;
        this.oldEdges = edge.oldEdges;
        this.isReplaceable = edge.isReplaceable;
        this.replaceableCost = edge.replaceableCost;
        this.replacingEdges = edge.replacingEdges;
        this.isMoved = false;
    }

    public Edge(int id, EdgeType edgeType, int cost, Node endpoint1, Node endpoint2, int originalId) {
        this.id = id;
        this.edgeType = edgeType;
        this.cost = cost;
        this.originalCost = cost;
        this.endNode1 = endpoint1;
        this.endNode2 = endpoint2;
        this.isUsed = false;
        this.originalID = originalId;
        this.oldEdges = new ArrayList<>();
        this.isReplaceable = false;
        this.replaceableCost = 0;
        this.replacingEdges = new ArrayList<>();
        this.isMoved = false;
    }

    public void Use(){
        this.isUsed = true;
        // Set each value of the old edges to true
        for (Edge edge : oldEdges) {
            edge.isUsed = true;
        }
    }

    @Override
    public String toString() {
        return String.format("Edge: %d-%d", endNode1.id, endNode2.id);
    }

    // Getters and setters can be added here if needed
    public void setReplaceable(boolean replaceable) {
        isReplaceable = replaceable;
    }
    public boolean isReplaceable() {
        return isReplaceable;
    }
    public void setMoved(boolean moved) {
        isMoved = moved;
    }
    public boolean isMoved() {
        return isMoved;
    }
    public void setReplaceableCost(int replaceableCost) {
        this.replaceableCost = replaceableCost;
    }
    public int getReplaceableCost() {
        return replaceableCost;
    }
    public void setReplacingEdges(List<Edge> replacingEdges) {
        this.replacingEdges = replacingEdges;
    }
    public List<Edge> getReplacingEdges() {
        return replacingEdges;
    }
}