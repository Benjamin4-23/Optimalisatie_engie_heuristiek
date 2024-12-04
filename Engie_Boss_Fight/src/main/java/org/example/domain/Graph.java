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
        transform();
        System.out.println("Number of nodes - edges before shaving: " + nodes.size() + "-" + edges.size());
        shave();
        clearNodes();
        System.out.println("Number of nodes-edges after shaving: " + nodes.size() + "-" + edges.size());
        simplify();
        System.out.println("Number of nodes - edges after simplification: " + nodes.size() + "-" + this.edges.size());
        //clearNodes();
        System.out.println("Number of nodes - edges after clearing: " + nodes.size() + "-" + this.edges.size());
    }

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

    public void transform(){
        int rootId = -1;
        Node rootNode = new Node(rootId, 0.0, 0.0, NodeType.REGULAR); // ID -1 for the virtual root

        int i = -1;
        Set<Integer> visitedNode = new HashSet<>();
        for (Edge edge: edges.values()){
            if(edge.edgeType == EdgeType.EXISTING){
                if(!visitedNode.add(edge.endNode1.id)){
                    Edge edgeFromRoot = new Edge(i, EdgeType.EXISTING, 0, rootNode, edge.endNode1, -1);
                    rootNode.edges.put(i--, edgeFromRoot);
                };
                if(!visitedNode.add(edge.endNode2.id)){
                    Edge edgeFromRoot = new Edge(i, EdgeType.EXISTING, 0, rootNode, edge.endNode2, -1);
                    rootNode.edges.put(i--, edgeFromRoot);
                };
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
        Set<Integer> visitedNodes = new HashSet<>();
        Set<Integer> nodesToRemove = new HashSet<>();


        int newEdgeId = edges.values().stream().mapToInt(edge -> edge.id).max().orElse(0);

        for (Node node : nodes.values()) {
            // Skip visited nodes or nodes that are not eligible for simplification
            if (visitedNodes.contains(node.id) || node.id == -1 || node.nodeType == NodeType.PROSPECT) {
                continue;
            }

            if (node.edges.size() == 2) { // One outgoing, one incoming? Merge them
                List<Edge> edges = new ArrayList<>(node.edges.values());
                Edge edge1 = edges.get(0);
                Edge edge2 = edges.get(1);

                // Get neighbors
                Node neighbor1 = edge1.endNode1.id == node.id ? edge1.endNode2 : edge1.endNode1;
                Node neighbor2 = edge2.endNode1.id == node.id ? edge2.endNode2 : edge2.endNode1;

                // Create new edge
                int combinedCost = edge1.cost + edge2.cost;
                Edge newEdge = new Edge(++newEdgeId, EdgeType.REGULAR, combinedCost, neighbor1, neighbor2, -1);

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

                nodesToRemove.add(node.id);
            }
        }

        this.edges = simplifiedEdges;
    }

    public void shave(){
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

                if(node.edges.size() == 1){
                    List<Edge> edges = new ArrayList<>(node.edges.values());
                    Edge edge = edges.get(0);


                    // Get neighbor
                    Node neighbor = edge.endNode1.id == node.id ? edge.endNode2 : edge.endNode1;

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

    private void clearNodes(){
        Set<Integer> visitedNodes = new HashSet<>();
        Collection<Edge> edges = nodes.get(-1).edges.values();
        List<Edge> removeEdges = new ArrayList<>();
        for (Node node : nodes.values()) {
            if (node.edges.isEmpty() && node.id != -1) {
                visitedNodes.add(node.id);
                for (Edge e: edges){
                    if(e.endNode2.id == node.id || e.endNode1.id == node.id){
                       removeEdges.add(e);
                    }
                }
            }
        }

        for (Edge e : removeEdges){
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

    public List<Node> findPath(Node start, Node end){
        Set<Node> vistedNode = new HashSet<>();
        Queue<List<Node>> queue = new LinkedList<>();

        queue.offer(new ArrayList<>(){{add(start);}});
        vistedNode.add(start);
        while (!queue.isEmpty()){
            List<Node> currentPath = queue.poll();
            Node currentNode = currentPath.get(currentPath.size()-1);

            if(currentNode==end){
                return currentPath;
            }

            if(!vistedNode.add(currentNode)) continue;

            for(Edge e : currentNode.edges.values()){
                Node nextNode = e.endNode2;
                if(vistedNode.contains(nextNode) && !e.isUsed) continue;

                List<Node> newPath = new ArrayList<>(currentPath);
                newPath.add(nextNode);
                queue.offer(newPath);

            }

        }
        return null;
    }

    public Map<Node, Double> dijkstra() {
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
            if(currentNode.nodeType == NodeType.PROSPECT) continue;

            // Process each edge of the current node
            for (Edge edge : currentNode.edges.values()) {
                Node neighbor = (edge.endNode1.id == currentNode.id) ? edge.endNode2 : edge.endNode1;

                double newCost = distances.get(currentNode.id) + edge.cost;

                // Relaxation step
                if (newCost < distances.get(neighbor.id)) {
                    distances.put(neighbor.id, newCost);
                    previousEdges.put(neighbor.id, edge);
                    pq.add(neighbor); // Re-insert to update priority
                }
            }
        }

        // Collect distances for prospects
        Map<Node, Double> prospectDistances = new HashMap<>();
        for (Node node : nodes.values()) {
            if (node.nodeType == NodeType.PROSPECT) {
                prospectDistances.put(node, distances.get(node.id));
            }
        }

        // Optional: Mark paths and edges used for prospects
        for (Node prospect : nodes.values()) {
            if (prospect.nodeType == NodeType.PROSPECT) {
                Node pathNode = prospect;
                while (previousEdges.containsKey(pathNode.id)) {
                    Edge pathEdge = previousEdges.get(pathNode.id);
                    pathEdge.use(); // Mark edge as used
                    pathNode = (pathEdge.endNode1 == pathNode) ? pathEdge.endNode2 : pathEdge.endNode1;
                }
            }
        }

        return prospectDistances;
    }


}