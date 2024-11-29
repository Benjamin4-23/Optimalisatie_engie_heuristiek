package org.example.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Graph {
    public HashMap<Integer, Node> nodes;
    public HashMap<Integer, Edge> edges;

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
        Map<Node, Double> distances = new HashMap<>();
        Node rootNode = nodes.get(-1); // id is -1 for the virtual root
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));
        Set<Node> visited = new HashSet<>();
        Map<Node, Edge> previousEdges = new HashMap<>(); // Track the edge used to reach each node

        // Initialize distances
        for (Node node : nodes.values()) {
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
                    previousEdges.put(neighbor, edge);
                }
            }

            // If the current node is a prospect, set the cost of the path edges to zero
            if (currentNode.nodeType == NodeType.PROSPECT) {
                Node pathNode = currentNode;
                while (previousEdges.containsKey(pathNode)) {
                    Edge pathEdge = previousEdges.get(pathNode);
                    pathEdge.cost = 0; // Set the cost to zero
                    pathNode = pathEdge.endNode1; // Move to the previous node in the path
                    pathEdge.Use();
                }
            }
        }

        // Filter distances to only include prospect nodes
        Map<Node, Double> prospectDistances = new HashMap<>();
        for (Node node : nodes.values()) {
            if (node.nodeType == NodeType.PROSPECT) {
                prospectDistances.put(node, distances.get(node));
            }
        }

        return prospectDistances;
    }
}