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

        int i = edges.size();
        for (Node node : nodes.values()) {
            if (node.nodeType != NodeType.PROSPECT) {
                Edge edgeFromRoot = new Edge(++i, EdgeType.EXISTING, 0, rootNode, node, -1);
                rootNode.edges.put(i, edgeFromRoot);
                //edges.put(edgeFromRoot.id, edgeFromRoot);
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
        System.out.println("Number of nodes - edges before simplification: " + nodes.size() + " - " + edges.size());

    }

    public void shave(){
        System.out.println("Number of nodes - edges before shaving: " + nodes.size() + " - " + edges.size());
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

        System.out.println("Number of nodes - edges after shaving: " + nodes.size() + " - " + edges.size());
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

    public Map<Node, Double> dijkstra(){
        Node startNode = nodes.get(-1);

        Map<Integer, Double> distances = new HashMap<>();
        Set<Integer> vistedNode = new HashSet<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> distances.getOrDefault(a, Double.POSITIVE_INFINITY)));
        Map<Node,Edge> previousEdges = new HashMap<>();

        //setup
        for (Node node: nodes.values()){
            distances.put(node.id, Double.POSITIVE_INFINITY);
        }
        distances.put(startNode.id, 0.0);
        pq.add(startNode);

        while (!pq.isEmpty()){
            Node currentNode = pq.poll();

            if(vistedNode.contains(currentNode.id)) continue;
            vistedNode.add(currentNode.id);

            for (Edge edge: currentNode.edges.values()){
                Node neighbour = edge.endNode2;

                if(vistedNode.contains(neighbour.id)) continue;

                double newCost = distances.get(currentNode.id) + edge.cost;

                //relaxatie
                if(newCost < distances.get(neighbour.id)){
                    distances.put(neighbour.id, newCost);
                    pq.add(neighbour);
                    previousEdges.put(neighbour, edge);
                }
            }

            // handle propects
            if(currentNode.nodeType == NodeType.PROSPECT){
                Node pathNode = currentNode;
                while (previousEdges.containsKey(pathNode)){
                    Edge pathEdge = previousEdges.get(pathNode);
                    pathEdge.cost = 0;
                    pathNode = pathEdge.endNode1;
                    pathEdge.Use();
                }
            }
        }
        Map<Node, Double> prospectDistances = new HashMap<>();
        for(Node node: nodes.values()){
            if(node.nodeType == NodeType.PROSPECT){
                prospectDistances.put(node, distances.get(node.id));
            }
        }
        return prospectDistances;
    }

}