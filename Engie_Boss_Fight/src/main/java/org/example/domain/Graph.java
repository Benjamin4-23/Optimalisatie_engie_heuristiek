package org.example.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Graph {
    public HashMap<Integer, Node> nodes;
    public HashMap<Integer, Edge> edges;
    public List<Integer> lockedEdges;
    public List<Integer> unlockedEdges;
    public Map<Integer, Edge> usedEdges;

    public Graph(HashMap<Integer, Node> nodes, HashMap<Integer, Edge> edges) {
        this.lockedEdges = new ArrayList<>();
        this.unlockedEdges = new ArrayList<>();
        this.usedEdges = new HashMap<>();
        this.nodes = nodes;
        this.edges = edges;
        transform();
        System.out.println("Number of nodes - edges before shaving: " + nodes.size() + "-" + edges.size());
        shave();
        clearNodes();
        System.out.println("Number of nodes-edges after shaving: " + nodes.size() + "-" + edges.size());
        simplify();
        System.out.println("Number of nodes-edges after simplification: " + this.nodes.size() + "-" + this.edges.size());

        shave();
        System.out.println("\nNumber of nodes-edges after second shaving: " + this.nodes.size() + "-" + this.edges.size());
        simplify();
        System.out.println("Number of nodes-edges after second simplification: " + this.nodes.size() + "-" + this.edges.size());

        StringBuilder jsonBuffer = new StringBuilder();
        jsonBuffer.append("{\"nodes\":[");
        for (Edge edge : this.edges.values()) {
            if (edge.endNode1.x != 0 && edge.endNode1.y != 0 && edge.endNode2.x != 0 && edge.endNode2.y != 0) {
                jsonBuffer.append(String.format("[%d,%d],", edge.endNode1.id, edge.endNode2.id));
            }
        }
        if (!this.edges.isEmpty()) {
            jsonBuffer.setLength(jsonBuffer.length() - 1);
        }
        jsonBuffer.append("]}");
        try {
            Files.write(Paths.get("./output/edges_after_shave.json"), jsonBuffer.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //lockEdges();
        System.out.println("\nLocked " + this.lockedEdges.size() + " prospect connections, " + this.unlockedEdges.size() + " unlocked edges left.");
    }

    public Graph(Graph other) {
        // Deep copy of nodes
        this.nodes = new HashMap<>();
        this.usedEdges = new HashMap<>(other.usedEdges);
        for (Integer key : other.nodes.keySet()) {
            this.nodes.put(key, new Node(other.nodes.get(key))); // Assuming Node has a copy constructor
        }

        // Deep copy of edges
        this.edges = new HashMap<>();
        for (Integer key : other.edges.keySet()) {
            this.edges.put(key, new Edge(other.edges.get(key))); // Assuming Edge has a copy constructor
        }

        this.lockedEdges = new ArrayList<>(other.lockedEdges);
        this.unlockedEdges = new ArrayList<>(other.unlockedEdges);
    }

    public void transform() {
        int rootId = -1;
        Node rootNode = new Node(rootId, 0.0, 0.0, NodeType.REGULAR); // ID -1 for the virtual root

        int i = -1;
        Set<Integer> visitedNode = new HashSet<>();
        for (Edge edge : edges.values()) {
            if (edge.edgeType == EdgeType.EXISTING) {
                if (!visitedNode.add(edge.endNode1.id)) {
                    Edge edgeFromRoot = new Edge(i, EdgeType.EXISTING, 0, rootNode, edge.endNode1);
                    rootNode.edges.put(i--, edgeFromRoot);
                }
                if (!visitedNode.add(edge.endNode2.id)) {
                    Edge edgeFromRoot = new Edge(i, EdgeType.EXISTING, 0, rootNode, edge.endNode2);
                    rootNode.edges.put(i--, edgeFromRoot);
                }
            }
        }

        nodes.put(rootId, rootNode);

        // Fill outgoingEdges in each node with edge that starts from that node and incomingEdges with edge that ends at that node
        for (Node node : nodes.values()) {
            for (Edge edge : edges.values()) {
                if (edge.endNode1 == node) {
                    node.edges.put(edge.id, edge);
                }
                if (edge.endNode2 == node) {
                    node.edges.put(edge.id, edge);
                }
            }
        }
    }

    public void simplify() {
        HashMap<Integer, Edge> simplifiedEdges = new HashMap<>(this.edges);

        int newEdgeId = edges.values().stream().mapToInt(edge -> edge.id).max().orElse(0);

        for (Node node : nodes.values()) {
            // Skip visited nodes or nodes that are not eligible for simplification
            if (node.id == -1 || node.nodeType == NodeType.PROSPECT) {
                continue;
            }

            List<Edge> removeEdges = new ArrayList<>();
            for (Edge e : node.edges.values()) {
                if (e.endNode2 == e.endNode1) removeEdges.add(e);
            }
            for (Edge e : removeEdges) {
                node.edges.remove(e.id);
            }

            if (node.edges.size() == 2) { // One outgoing, one incoming? Merge them
                List<Edge> edges = new ArrayList<>(node.edges.values());
                Edge edge1 = edges.get(0);
                Edge edge2 = edges.get(1);

                // Get neighbors
                Node neighbor1 = edge1.getOtherNode(node.id);
                Node neighbor2 = edge2.getOtherNode(node.id);

                // Create new edge
                int combinedCost = edge1.cost + edge2.cost;
                Edge newEdge = new Edge(++newEdgeId, EdgeType.REGULAR, combinedCost, neighbor1, neighbor2);

                if (edge1.oldEdges.isEmpty()) {
                    newEdge.oldEdges.add(edge1);
                } else {
                    newEdge.oldEdges.addAll(edge1.oldEdges);
                }

                if (edge2.oldEdges.isEmpty()) {
                    newEdge.oldEdges.add(edge2);
                } else {
                    newEdge.oldEdges.addAll(edge2.oldEdges);
                }

                simplifiedEdges.put(newEdge.id, newEdge);

                // Update neighbors
                neighbor1.removeEdgeWithNode(node);
                neighbor1.edges.put(newEdge.id, newEdge);

                neighbor2.removeEdgeWithNode(node);
                neighbor2.edges.put(newEdge.id, newEdge);

                // Remove edges from the graph
                simplifiedEdges.remove(edge1.id);
                simplifiedEdges.remove(edge2.id);

                // Clear edges of the current node
                node.edges.clear();
            }
        }

        this.edges = simplifiedEdges;
    }

    public void shave() {
        HashMap<Integer, Edge> simplifiedEdges = new HashMap<>(edges);
        int numberOfEdgesRemoved;
        do {
            numberOfEdgesRemoved = 0;
            Set<Integer> visitedNodes = new HashSet<>();
            for (Node node : nodes.values()) {
                // Skip nodes that are already visited or are not eligible for simplification
                if (visitedNodes.contains(node.id) || node.id == -1 || node.nodeType == NodeType.PROSPECT) {
                    continue;
                }

                if (node.edges.size() == 1) {
                    List<Edge> edges = new ArrayList<>(node.edges.values());
                    Edge edge = edges.get(0);


                    // Get neighbor
                    Node neighbor = edge.getOtherNode(node.id);

                    // Remove edges in the outgoing/ incoming edges list of the neighbor
                    neighbor.removeEdgeWithNode(node);

                    node.edges.clear();

                    simplifiedEdges.remove(edge.id);

                    numberOfEdgesRemoved++;

                    visitedNodes.add(node.id);
                }
            }

        } while (numberOfEdgesRemoved > 0);

        this.edges = simplifiedEdges;


    }

    /*private void lockEdges() {
        for (Edge edge : edges.values()) {
            Node node1 = edge.endNode1;
            Node node2 = edge.endNode2;

            if (edge.edgeType == EdgeType.EXISTING || node1.nodeType == NodeType.PROSPECT || node2.nodeType == NodeType.PROSPECT) {
                // Only lock when the prospect node has only one edge
                if (node1.nodeType == NodeType.PROSPECT && node1.edges.size() == 1) {
                    lockEdge(edge);
                    this.lockedEdges.add(edge.id);
                } else if (node2.nodeType == NodeType.PROSPECT && node2.edges.size() == 1) {
                    lockEdge(edge);
                    this.lockedEdges.add(edge.id);

                }
            } else {
                edge.unlock();
                this.unlockedEdges.add(edge.id);
            }
        }
    }*/

    private void clearNodes() {
        Set<Integer> visitedNodes = new HashSet<>();
        Collection<Edge> edges = nodes.get(-1).edges.values();
        List<Edge> removeEdges = new ArrayList<>();
        for (Node node : nodes.values()) {
            if (node.edges.isEmpty() && node.id != -1) {
                visitedNodes.add(node.id);
                for (Edge e : edges) {
                    if (e.endNode2.id == node.id || e.endNode1.id == node.id) {
                        removeEdges.add(e);
                    }
                }
            }
        }

        for (Edge e : removeEdges) {
            edges.remove(e);
        }

        for (Integer id : visitedNodes) {
            nodes.remove(id);
        }
    }

    // Deep copy via clone method
    @Override
    protected Object clone() {
        return new Graph(this);
    }


    public Double dijkstra() {
        double cost = 0.0;
        // Make sure all edges are unused
        this.usedEdges.clear();

        Node rootNode = nodes.get(-1);
        Map<Integer, Double> distances = new HashMap<>();
        Map<Integer, Edge> previousEdges = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(node -> distances.getOrDefault(node.id, Double.POSITIVE_INFINITY)));

        // Initialize distances
        for (Node node : nodes.values()) {
            distances.put(node.id, Double.POSITIVE_INFINITY);
        }
        distances.put(rootNode.id, 0.0);
        pq.add(rootNode);

        while (!pq.isEmpty()) {
            Node currentNode = pq.poll();
            if (currentNode.nodeType == NodeType.PROSPECT) continue;

            // Process each edge of the current node
            for (Edge edge : currentNode.edges.values()) {
                if(edge.isBlocked) continue;

                Node neighbor = edge.getOtherNode(currentNode.id);

                double newCost = distances.get(currentNode.id) + edge.cost;

                // Relaxation step
                if (newCost < distances.get(neighbor.id)) {
                    distances.put(neighbor.id, newCost);
                    previousEdges.put(neighbor.id, edge);
                    pq.add(neighbor); // Re-insert to update priority
                }
            }
        }

        for (Node prospect : nodes.values()) {
            if (prospect.nodeType == NodeType.PROSPECT) {
                Node pathNode = prospect;
                while (previousEdges.containsKey(pathNode.id)) {
                    Edge pathEdge = previousEdges.get(pathNode.id);
                    pathEdge.use(); // Mark edge as used
                    if (pathEdge.endNode1.id != rootNode.id && pathEdge.endNode2.id != rootNode.id) {
                        this.usedEdges.put(pathEdge.id, pathEdge);
                    }
                    pathNode = pathEdge.getOtherNode(pathNode.id);
                }
            }
        }
        for (Node n : nodes.values()){
            if(n.nodeType == NodeType.PROSPECT){
                if(distances.get(n.id)==Double.POSITIVE_INFINITY){
                    return Double.POSITIVE_INFINITY;
                }
            }
        }

        // Calculate total cost
        for (Edge edge : usedEdges.values()) {
            cost += edge.cost;
        }


        return cost;
    }

}