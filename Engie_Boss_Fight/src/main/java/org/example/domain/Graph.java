package org.example.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class Graph {
    public HashMap<Integer, Node> nodes;
    public HashMap<Integer, Edge> edges;
    public Node rootNode;

    public Graph(HashMap<Integer, Node> nodes, HashMap<Integer, Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public void addNode() {
        // Implementation for adding a node
    }

    public void addEdge() {
        // Implementation for adding an edge
    }

    // Deep copy constructor
    public Graph(Graph other) {
        // Deep copy of nodes
        this.nodes = new HashMap<>();
        for (Integer key : other.nodes.keySet()) {
            this.nodes.put(key, new Node(other.nodes.get(key))); // Assuming Node has a copy constructor
        }

        // Deep copy of edges
        this.edges = new HashMap<>();
        for (Integer key : other.edges.keySet()) {
            this.edges.put(key, new Edge(other.edges.get(key))); // Assuming Edge has a copy constructor
        }
    }

    // Deep copy via clone method
    @Override
    protected Object clone() {
        return new Graph(this);
    }
    public Map<Node, Double> dijkstraFromRootToProspects() {
        // Create copies of nodes and edges
        HashMap<Integer, Node> nodeCopies = new HashMap<>();
        HashMap<Integer, Edge> edgeCopies = new HashMap<>();
        for (Node node : nodes.values()) {
            nodeCopies.put(node.id, new Node(node.id, node.x, node.y, node.nodeType));
        }
        for (Edge edge : edges.values()) {
            Node endNode1Copy = nodeCopies.get(edge.endNode1.id);
            Node endNode2Copy = nodeCopies.get(edge.endNode2.id);
            edgeCopies.put(edge.id, new Edge(edge.id, edge.edgeType, edge.cost, endNode1Copy, endNode2Copy));
        }

        Map<Node, Double> distances = new HashMap<>();
        Node rootNode = nodeCopies.get(-1); // id is -1 for the virtual root
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));
        Set<Node> visited = new HashSet<>();
        List<Edge> usedEdges = new ArrayList<>();

        // Initialize distances
        for (Node node : nodeCopies.values()) {
            distances.put(node, Double.POSITIVE_INFINITY);
        }
        distances.put(rootNode, 0.0);
        priorityQueue.add(rootNode);

        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();
            if (!visited.add(currentNode)) continue;

            for (Edge edge : currentNode.outgoingEdges.values()) {
                Node neighbor = edge.endNode2; // Assuming endNode1 is the source and endNode2 is the target
                if (visited.contains(neighbor)) continue;

                double newDist = distances.get(currentNode) + edge.cost;
                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    priorityQueue.add(neighbor);
                    usedEdges.add(edge);
                }
            }
        }

        // Set the cost of used edges to zero
        for (Edge edge : usedEdges) {
            edgeCopies.get(edge.id).cost = 0;
        }

        // Filter distances to only include prospect nodes
        Map<Node, Double> prospectDistances = new HashMap<>();
        for (Node node : nodeCopies.values()) {
            if (node.nodeType == Node.NodeType.PROSPECT) {
                prospectDistances.put(node, distances.get(node));
            }
        }

        return prospectDistances;
    }

    // Getters and setters can be added here if needed
}