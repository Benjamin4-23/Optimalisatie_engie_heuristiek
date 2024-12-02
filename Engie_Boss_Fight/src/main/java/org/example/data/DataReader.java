package org.example.data;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.example.domain.Edge;
import org.example.domain.EdgeType;
import org.example.domain.Node;
import org.example.domain.NodeType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class DataReader {
    private String filepath;
    private HashMap<Integer, Node> nodes;
    private HashMap<Integer, Edge> edges;
    private HashMap<Integer, Edge> loopEdges;
    private Set<Node> existingNodes = new HashSet<>();
    private Node rootNode;


    public HashMap<Integer, Node> originalNodes = new HashMap<>();
    public HashMap<Integer, Edge> originalEdges = new HashMap<>();
    public HashMap<Integer, Node> simplifiedNodes = new HashMap<>();
    public HashMap<Integer, Edge> simplifiedEdges = new HashMap<>();
    public HashMap<Integer, Node> shavedNodes = new HashMap<>();
    public HashMap<Integer, Edge> shavedEdges = new HashMap<>();

    // Constructor
    public DataReader(String filepath) {
        this.filepath = filepath;
        this.nodes = new HashMap<>();
        this.edges = new HashMap<>();
        this.loopEdges = new HashMap<>();
    }

    public HashMap<Integer, Node> getNodes() {
        return nodes;
    }

    public HashMap<Integer, Edge> getEdges() {
        return edges;
    }

    public HashMap<Integer, Edge> getLoopEdges() {
        return loopEdges;
    }

    // Load and parse data from JSON file
    public void loadData() {
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(filepath)) {
            JSONObject data = (JSONObject) parser.parse(reader);
            JSONArray nodesArray = (JSONArray) data.get("nodes");

            for (Object obj : nodesArray) {
                JSONObject nodeObj = (JSONObject) obj;
                int id = ((Long) nodeObj.get("id")).intValue();
                JSONArray coordsArray = (JSONArray) nodeObj.get("coords");
                double x = (double) coordsArray.get(0);
                double y = (double) coordsArray.get(1);

                String type = (String) nodeObj.get("node_type");
                nodes.put(id, new Node(id, x, y, NodeType.valueOf(type.toUpperCase())));
            }

            JSONArray edgesArray = (JSONArray) data.get("edges");

            for (Object obj : edgesArray) {
                JSONObject EdgeObj = (JSONObject) obj;
                int id = ((Long) EdgeObj.get("id")).intValue();
                String type = (String) EdgeObj.get("edge_type");
                int cost = ((Long) EdgeObj.get("cost")).intValue();
                int endpoint1 = ((Long) EdgeObj.get("endpoint1")).intValue();
                int endpoint2 = ((Long) EdgeObj.get("endpoint2")).intValue();

                Node endNode1 = nodes.get(endpoint1);
                Node endNode2 = nodes.get(endpoint2);

                edges.put(id, new Edge(id, EdgeType.valueOf(type.toUpperCase()), cost, endNode1, endNode2, -1));
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
    public void transform(){
        // Step 1: Convert undirected edges to directed edges
        List<Edge> directedEdges = new ArrayList<>();
        int i = 0;
        for (Edge edge : edges.values()) {
            if(edge.edgeType == EdgeType.EXISTING){
                existingNodes.add(edge.endNode1);
                existingNodes.add(edge.endNode2);
            }
            if (nodes.get(edge.endNode1.id).nodeType == NodeType.PROSPECT ||
                    nodes.get(edge.endNode2.id).nodeType == NodeType.PROSPECT) {
                // For edges involving a prospect, add a single directed edge towards the prospect
                if (nodes.get(edge.endNode1.id).nodeType == NodeType.PROSPECT) {
                    directedEdges.add(new Edge(i++, edge.edgeType, edge.cost, edge.endNode2, edge.endNode1, edge.id));
                } else {
                    directedEdges.add(new Edge(i++, edge.edgeType, edge.cost, edge.endNode1, edge.endNode2, edge.id));
                }
            } else {
                // For regular nodes, add two directed edges
                directedEdges.add(new Edge(i++, edge.edgeType, edge.cost, edge.endNode1, edge.endNode2, edge.id));
                directedEdges.add(new Edge(i++, edge.edgeType, edge.cost, edge.endNode2, edge.endNode1, edge.id));
            }
        }
        edges.clear();
        for (Edge edge : directedEdges) {
            edges.put(edge.id, edge);
        }

        // Step 2: Add a virtual root node
        int rootId = -1;
        rootNode = new Node(rootId, 0.0, 0.0, NodeType.REGULAR); // ID -1 for the virtual root
        nodes.put(rootId, rootNode);

        // Connect the root node to all existing non-prospect nodes in the network with cost 0
        for (Node node : existingNodes) {
            if (node.nodeType != NodeType.PROSPECT) {
                Edge edgeFromRoot = new Edge(i++, EdgeType.EXISTING, 0, rootNode, node, -1);
                edges.put(edgeFromRoot.id, edgeFromRoot);
            }
        }

        // Fill outgoingEdges in each node with edge that starts from that node and incomingEdges with edge that ends at that node
        for (Node node : nodes.values()) {
            for (Edge edge : edges.values()) {
                if (edge.endNode1 == node) {
                    node.outgoingEdges.put(edge.id, edge);
                }
                if (edge.endNode2 == node) {
                    node.incomingEdges.put(edge.id, edge);
                }
            }
        }
        this.originalNodes = new HashMap<>(nodes);
        this.originalEdges = new HashMap<>(edges);
    }

    public void simplify() {
        System.out.println("Number of nodes - edges before simplification: " + nodes.size() + " - " + edges.size());

        HashMap<Integer, Edge> simplifiedEdges = new HashMap<>(edges);
        boolean changes;
        do {
            changes = false;
            // Find nodes with exactly 2 connections (4 edges due to bidirectional)
            Set<Node> simplifiableNodes = new HashSet<>();
            for (Node node : nodes.values()) {
                if (node.outgoingEdges.size() == 2 && node.incomingEdges.size() == 2) {
                    // Check if these are 2 bidirectional connections to 2 neighbors
                    boolean connectedToNodes = true;
                    int neighborCount = 0;
                    for (Edge edge : node.outgoingEdges.values()) {
                        Node neighbor = edge.endNode2;
                        if (neighbor.nodeType == NodeType.PROSPECT || neighbor.id == rootNode.id) {
                            connectedToNodes = false;
                            break;
                        }
                        if (neighbor.outgoingEdges.values().stream().anyMatch(e -> e.endNode2.id == node.id)) {
                            neighborCount++;
                        }
                    }
                    if (connectedToNodes && neighborCount == 2) {
                        simplifiableNodes.add(node);
                    }
                }
            }
            // Find boundary nodes (non-simplifiable nodes connected to simplifiable nodes)
            List<Node> boundaryNodes = new ArrayList<>();
            for (Node node : nodes.values()) {
                if (!simplifiableNodes.contains(node)) {
                    // Check if any outgoing edges lead to simplifiable nodes
                    boolean hasSimplifiableConnection = node.outgoingEdges.values().stream()
                        .anyMatch(e -> simplifiableNodes.contains(e.endNode2));

                    if (hasSimplifiableConnection && node.nodeType != NodeType.PROSPECT && node.id != rootNode.id) {
                        if (!boundaryNodes.contains(node)) {
                            boundaryNodes.add(node);
                        }
                    }
                }
            }
            // For a boundary node, find path to another boundary node

            if (boundaryNodes.size() == 0) {
                break;
            }
            Node startNode = boundaryNodes.get(0);
            Edge startEdge = null;
            for (Edge edge : startNode.outgoingEdges.values()) {
                if (simplifiableNodes.contains(edge.endNode2)) {
                    startEdge = edge;
                    break;
                }
            }
            if (startEdge == null) {
                System.out.println("startEdge is null");
                return;
            }
            Node currentNode = startEdge.endNode2;
            List<Edge> pathEdges = new ArrayList<>();
            pathEdges.add(startEdge);


            // Follow the path until we reach a non-simplifiable node
            while (simplifiableNodes.contains(currentNode)) {
                Edge nextEdge = currentNode.outgoingEdges.values().stream()
                    .filter(e -> e.endNode2.id != pathEdges.get(pathEdges.size() - 1).endNode1.id)
                    .findFirst()
                    .get();
                pathEdges.add(nextEdge);
                currentNode = nextEdge.endNode2;
            }

            // pad van langer dan 1 gevonden
            if (pathEdges.size() > 1) {
                // Create new simplified edges (bidirectional)
                int totalCost = pathEdges.stream()
                    .mapToInt(e -> e.cost)
                    .sum();

                int newID =  edges.values().stream().mapToInt(edge -> edge.id).max().orElse(0) + 1;
                Edge newEdge1 = new Edge(
                    newID+1,
                    EdgeType.REGULAR,
                    totalCost,
                    startNode,
                    currentNode,
                    newID+1
                );
                newEdge1.oldEdges.addAll(pathEdges);
                // Remove old edges and add new one
                for (Edge oldEdge : pathEdges) {
                    if (!simplifiedEdges.containsKey(oldEdge.id)) {
                        System.out.println("oldEdge not in simplifiedEdges: " + oldEdge.id);
                    }
                    simplifiedEdges.remove(oldEdge.id);
                }
                simplifiedEdges.put(newEdge1.id, newEdge1);

                Edge newEdge2 = new Edge(
                    newID+2,
                    EdgeType.REGULAR,
                    totalCost,
                    currentNode,
                    startNode,
                    newID+2
                );
                List<Edge> oldEdges = new ArrayList<>();
                for (Edge oldEdge : pathEdges) {
                    Edge oldEdgeReverse = simplifiedEdges.values().stream()
                        .filter(e -> e.endNode1.id == oldEdge.endNode2.id && e.endNode2.id == oldEdge.endNode1.id)
                        .findFirst()
                        .orElse(null);
                    if (oldEdgeReverse == null) {
                        System.out.println("oldEdgeReverse not found: " + oldEdge.id);
                    }
                    oldEdges.add(oldEdgeReverse);
                    simplifiedEdges.remove(oldEdgeReverse.id);
                }
                newEdge2.oldEdges.addAll(oldEdges);
                simplifiedEdges.put(newEdge2.id, newEdge2);

                changes = true;
            }

            if (changes) {
                // Update the edges map with simplified edges
                edges = simplifiedEdges;
                // Update node connections
                for (Node node : nodes.values()) {
                    node.outgoingEdges.clear();
                    node.incomingEdges.clear();
                }
                // Rebuild node connections with new edges
                for (Edge edge : edges.values()) {
                    if (edge.endNode1 != edge.endNode2) {
                        edge.endNode1.outgoingEdges.put(edge.id, edge);
                        edge.endNode2.incomingEdges.put(edge.id, edge);
                    }
                    else {
                        loopEdges.put(edge.id, edge);
                    }
                }
            }
        } while (changes); // Continue until no more simplifications can be made

        System.out.println("Number of nodes - edges after simplification: " + nodes.size() + " - " + edges.size());
        this.simplifiedNodes = new HashMap<>(nodes);
        this.simplifiedEdges = new HashMap<>(edges);
    }

    /*public void simplify() {
        System.out.println("Number of nodes - edges before simplification: " + nodes.size() + " - " + edges.size());


        HashMap<Integer, Edge> simplifiedEdges = new HashMap<>(edges);
        Set<Integer> visitedNodes = new HashSet<>();

        // Get highest edge id
        int newEdgeId = edges.values().stream().mapToInt(edge -> edge.id).max().orElse(0);

        for (Node node : nodes.values()) {
            // Skip nodes that are already visited or are not eligible for simplification
             if (visitedNodes.contains(node.id) || node.id == rootNode.id) {
                continue;
            }

            // We seek one node that is eligible for simplification.
            // A node is eligible for simplification if it has exactly 1 incoming edge and 1 outgoing edge (2 since it is bidirectional).
            if (node.incomingEdges.size() == 2 && node.outgoingEdges.size() == 2) {
                // This node is getting removed from the graph, so we need to get the edge that is incoming and outgoing to calculate the cost
                List<Edge> incomingEdges = new ArrayList<>(node.incomingEdges.values());
                List<Edge> outgoingEdges = new ArrayList<>(node.outgoingEdges.values());

                Edge incomingEdge = incomingEdges.get(0);
                Edge outgoingEdge = outgoingEdges.stream()
                        .filter(e -> !incomingEdge.endNode1.equals(e.endNode2))
                        .findFirst()
                        .orElse(null);

                Edge incomingEdge2 = incomingEdges.get(1);
                Edge outgoingEdge2 = outgoingEdges.stream()
                        .filter(e -> !incomingEdge2.endNode1.equals(e.endNode2))
                        .findFirst()
                        .orElse(null);

                if (outgoingEdge == null || outgoingEdge2 == null) {
                    continue; // Skip this node if no distinct neighbor is found
                }

                int combinedCost = incomingEdge.cost + outgoingEdge.cost;

                // Get neighbors
                Node neighbor1 = incomingEdge.endNode1;
                Node neighbor2 = outgoingEdge.endNode2;

                // Remove edges in the outgoing/ incoming edges list of the neighbors
                neighbor1.removeEdgesWithNode(node);
                neighbor2.removeEdgesWithNode(node);

                // Add new edge to both neighbors
                int id = newEdgeId++;
                while (simplifiedEdges.get(id) != null) {
                    id = newEdgeId++;
                }
                Edge oneToTwo = new Edge(id, EdgeType.REGULAR, combinedCost, neighbor1, neighbor2, id);
                if(Math.abs(incomingEdge.endNode1.id - incomingEdge.endNode2.id) == 1) {
                    oneToTwo.oldEdges.add(incomingEdge);
                }
                if (Math.abs(outgoingEdge.endNode2.id - outgoingEdge.endNode1.id) == 1) {
                    oneToTwo.oldEdges.add(outgoingEdge);
                }

                oneToTwo.oldEdges.addAll(getOldEdges(incomingEdge.oldEdges));
                oneToTwo.oldEdges.addAll(getOldEdges(outgoingEdge.oldEdges));

                id = newEdgeId++;
                while (simplifiedEdges.get(id) != null) {
                    id = newEdgeId++;
                }
                Edge twoToOne = new Edge(id, EdgeType.REGULAR, combinedCost, neighbor2, neighbor1, id);
                if(Math.abs(incomingEdge2.endNode1.id - incomingEdge2.endNode2.id) == 1) {
                    twoToOne.oldEdges.add(incomingEdge2);
                }
                if (Math.abs(outgoingEdge2.endNode2.id - outgoingEdge2.endNode1.id) == 1) {
                    twoToOne.oldEdges.add(outgoingEdge2);
                }

                twoToOne.oldEdges.addAll(incomingEdge2.oldEdges);
                twoToOne.oldEdges.addAll(outgoingEdge2.oldEdges);

                // Add the new edges to the neighbors
                neighbor1.outgoingEdges.put(oneToTwo.id, oneToTwo);
                neighbor1.incomingEdges.put(twoToOne.id, twoToOne);
                neighbor2.outgoingEdges.put(twoToOne.id, twoToOne);
                neighbor2.incomingEdges.put(oneToTwo.id, oneToTwo);

                // Add the new edges to the simplified edges
                simplifiedEdges.put(oneToTwo.id, oneToTwo);
                simplifiedEdges.put(twoToOne.id, twoToOne);

                // Remove the edges from the simplified edges
                for (Edge edge : node.incomingEdges.values()) {
                    simplifiedEdges.remove(edge.id);
                }
                for (Edge edge : node.outgoingEdges.values()) {
                    simplifiedEdges.remove(edge.id);
                }
                node.outgoingEdges.clear();
                node.incomingEdges.clear();

                visitedNodes.add(node.id);
            }
        }
        this.edges = simplifiedEdges;
        System.out.println("Number of nodes - edges after simplification: " + nodes.size() + " - " + edges.size());
        this.simplifiedNodes = new HashMap<>(nodes);
        this.simplifiedEdges = new HashMap<>(edges);
    }

    private List<Edge> getOldEdges(List<Edge> edges) {
        List<Edge> oldEdges = new ArrayList<>();
        for(Edge e : edges) {
            if(e.oldEdges.isEmpty()) {
                oldEdges.add(e);
            } else{
                oldEdges.addAll(getOldEdges(e.oldEdges));
                break;
            }
        }
        return oldEdges;
    }*/


    public void shave(){
        System.out.println("Number of nodes - edges before shaving: " + nodes.size() + " - " + edges.size());
        HashMap<Integer, Edge> simplifiedEdges = new HashMap<>(edges);
        int numberOfEdgesRemoved;
        do {
            numberOfEdgesRemoved = 0;
            Set<Integer> visitedNodes = new HashSet<>();
            for (Node node : nodes.values()) {
                // Skip nodes that are already visited or are not eligible for simplification
                if (visitedNodes.contains(node.id) || node.id == rootNode.id || node.nodeType == NodeType.PROSPECT) {
                    continue;
                }

                if(node.outgoingEdges.size() == 1 && node.incomingEdges.size() == 1){
                    List<Edge> incomingEdges = new ArrayList<>(node.incomingEdges.values());
                    List<Edge> outgoingEdges = new ArrayList<>(node.outgoingEdges.values());

                    Edge incomingEdge = incomingEdges.get(0);
                    Edge outgoingEdge = outgoingEdges.get(0);

                    // Get neighbor
                    Node neighbor = outgoingEdge.endNode2;

                    // Remove edges in the outgoing/ incoming edges list of the neighbor
                    neighbor.removeEdgesWithNode(node);

                    node.incomingEdges.clear();
                    node.outgoingEdges.clear();

                    simplifiedEdges.remove(incomingEdge.id);
                    simplifiedEdges.remove(outgoingEdge.id);
                    numberOfEdgesRemoved += 2;

                    visitedNodes.add(node.id);
                }
            }

        } while (numberOfEdgesRemoved > 0);

        this.edges = simplifiedEdges;

        System.out.println("Number of nodes - edges after shaving: " + nodes.size() + " - " + edges.size());
        this.shavedNodes = new HashMap<>(nodes);
        this.shavedEdges = new HashMap<>(edges);
    }
}
