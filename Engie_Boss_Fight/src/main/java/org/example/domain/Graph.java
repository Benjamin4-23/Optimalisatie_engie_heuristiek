package org.example.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Graph {
    public HashMap<Integer, Node> nodes;
    public HashMap<Integer, Edge> edges;
    public Set<Integer> lockedEdges;
    public Set<Integer> unlockedEdges;

    public Graph(HashMap<Integer, Node> nodes, HashMap<Integer, Edge> edges) {
        this.lockedEdges = new HashSet<>();
        this.unlockedEdges = new HashSet<>();
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

        lockEdges();
        System.out.println("\nLocked " + this.lockedEdges.size() + " prospect connections, " + this.unlockedEdges.size() + " unlocked edges left.");
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
                    Edge edgeFromRoot = new Edge(i, EdgeType.EXISTING, 0, rootNode, edge.endNode1);
                    rootNode.edges.put(i--, edgeFromRoot);
                };
                if(!visitedNode.add(edge.endNode2.id)){
                    Edge edgeFromRoot = new Edge(i, EdgeType.EXISTING, 0, rootNode, edge.endNode2);
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

        int newEdgeId = edges.values().stream().mapToInt(edge -> edge.id).max().orElse(0);

        for (Node node : nodes.values()) {
            // Skip visited nodes or nodes that are not eligible for simplification
            if (node.id == -1 || node.nodeType == NodeType.PROSPECT) {
                continue;
            }

            List<Edge> removeEdges = new ArrayList<>();
            for (Edge e: node.edges.values()){
                if(e.endNode2 == e.endNode1) removeEdges.add(e);
            }
            for (Edge e: removeEdges){
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

    private void lockEdges(){
        for (Edge edge : edges.values()) {
            Node node1 = edge.endNode1;
            Node node2 = edge.endNode2;

            if (edge.edgeType == EdgeType.EXISTING || node1.nodeType == NodeType.PROSPECT || node2.nodeType == NodeType.PROSPECT) {
                edge.lock();
                this.lockedEdges.add(edge.id);
            }else{
                edge.unlock();
                this.unlockedEdges.add(edge.id);
            }
        }
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

    public Map<Node, Double> reConnect() {
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

    public static List<List<Edge>> BFS(Node start, Node end) {
        //return all unique paths
        List<List<Edge>> allPaths = new ArrayList<>();
        Queue<List<Edge>> queue = new LinkedList<>();
        queue.add(new ArrayList<>());

        while (!queue.isEmpty()) {
            List<Edge> currentPath = queue.poll();
            Node currentNode = currentPath.isEmpty() ? start : currentPath.get(currentPath.size() - 1).getOtherNode(start.id);

            if (currentNode.id == end.id) {
                allPaths.add(new ArrayList<>(currentPath));
                continue;
            }

            for (Edge edge : currentNode.edges.values()) {
                Node neighbor = edge.getOtherNode(currentNode.id);

                if (!isNodeInPath(currentPath, neighbor.id)) {
                    List<Edge> newPath = new ArrayList<>(currentPath);
                    newPath.add(edge);
                    queue.add(newPath);
                }
            }
        }

        return allPaths;
    }

    private static boolean isNodeInPath(List<Edge> path, int nodeId) {
        for (Edge edge : path) {
            if (edge.endNode1.id == nodeId || edge.endNode2.id == nodeId) {
                return true;
            }
        }
        return false;
    }

    public static Path<Edge> findShortestPath(Node start, Node end) {
        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Edge> previousEdge = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(distances::get));

        distances.put(start, 0.0);
        pq.add(start);

        while (!pq.isEmpty()) {
            Node currentNode = pq.poll();

            if (currentNode == end) {
                // Construct the path
                Path<Edge> path = new Path();
                int unusedCost = 0;
                Node pathNode = end;
                while (previousEdge.containsKey(pathNode)) {
                    Edge edge = previousEdge.get(pathNode);
                    if(!edge.isUsed){
                        unusedCost += edge.cost;
                    }
                    path.add(0, edge);
                    pathNode = edge.getOtherNode(pathNode.id);
                }
                path.cost = unusedCost;
                return path;
            }

            for (Edge edge : currentNode.edges.values()) {
                Node neighbor = edge.getOtherNode(currentNode.id);
                double newDist = distances.getOrDefault(currentNode, Double.POSITIVE_INFINITY) + edge.cost;

                if (newDist < distances.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    distances.put(neighbor, newDist);
                    previousEdge.put(neighbor, edge);
                    pq.add(neighbor);
                }
            }
        }

        // No path found
        return Path.empty();
    }


}