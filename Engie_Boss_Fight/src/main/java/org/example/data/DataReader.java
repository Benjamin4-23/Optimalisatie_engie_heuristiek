package org.example.data;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.example.domain.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class DataReader {
    private String filepath;
    private HashMap<Integer, Node> nodes;
    private HashMap<Integer, Edge> edges;
    private Set<Node> existingNodes = new HashSet<>();
    private Node rootNode;

    // Constructor
    public DataReader(String filepath) {
        this.filepath = filepath;
        this.nodes = new HashMap<>();
        this.edges = new HashMap<>();
    }

    public HashMap<Integer, Node> getNodes() {
        return nodes;
    }

    public HashMap<Integer, Edge> getEdges() {
        return edges;
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
    }

    public void simplify() {
        System.out.println("Number of edges before simplification: " + edges.size());
        System.out.println("Number of nodes before simplification: " + nodes.size());

        HashMap<Integer, Edge> simplifiedEdges = new HashMap<>(edges);
        Set<Integer> visitedNodes = new HashSet<>();

        // Get highest edge id
        int newEdgeId = edges.values().stream().mapToInt(edge -> edge.id).max().orElse(0);

        for (Node node : nodes.values()) {
            // Skip nodes that are already visited or are not eligible for simplification
            if (visitedNodes.contains(node.id) || node.nodeType == NodeType.PROSPECT || node.id == rootNode.id) {
                continue;
            }
            /*
            We seek one node that is eligible for simplification.
            A node is eligible for simplification if it has exactly 1 incoming edge and 1 outgoing edge (2 since it is bidirectional).
             */
            // A node is eligible for simplification if it has exactly 1 incoming edge and 1 outgoing edge
            if (node.incomingEdges.size() == 2 && node.outgoingEdges.size() == 2) {
                // This node is getting removed from the graph, so we need to get the edge that is incoming and outgoing to calculate the cost
                List<Edge> incomingEdges = new ArrayList<>(node.incomingEdges.values());
                List<Edge> outgoingEdges = new ArrayList<>(node.outgoingEdges.values());

                Edge incomingEdge = incomingEdges.get(0);
                Edge outgoingEdge = outgoingEdges.get(0);

                if(incomingEdge.endNode1.equals(outgoingEdge.endNode2)){
                    outgoingEdge = outgoingEdges.get(1); // We want to make sure we're not looking at the same neighbor
                }

                // Combine the edges
                int combinedCost = incomingEdge.cost + outgoingEdge.cost;

                // Get neighbors
                Node neighbor1 = incomingEdge.endNode1;
                Node neighbor2 = outgoingEdge.endNode2;

                // Remove edges in the outgoing/ incoming edges list of the neighbors
                neighbor1.removeEdgesWithNode(node);
                neighbor2.removeEdgesWithNode(node);

                // Add new edge to both neighbors
                // from neighbor1 to neighbor2
                int id = newEdgeId++;
                while(simplifiedEdges.get(id) != null){
                    id = newEdgeId++;
                }
                Edge oneToTwo = new Edge(id, incomingEdge.edgeType, combinedCost, neighbor1, neighbor2, id);
                oneToTwo.oldEdges.add(incomingEdge);
                oneToTwo.oldEdges.add(outgoingEdge);

                id = newEdgeId++;
                while(simplifiedEdges.get(id) != null){
                    id = newEdgeId++;
                }
                Edge twoToOne = new Edge(id, incomingEdge.edgeType, combinedCost, neighbor2, neighbor1, id);
                twoToOne.oldEdges.add(incomingEdge);
                twoToOne.oldEdges.add(outgoingEdge);

                // Add the new edges to the neighbors
                neighbor1.outgoingEdges.put(oneToTwo.id, oneToTwo);
                neighbor1.incomingEdges.put(twoToOne.id, twoToOne);
                neighbor2.outgoingEdges.put(twoToOne.id, twoToOne);
                neighbor2.incomingEdges.put(oneToTwo.id, oneToTwo);

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

        // print all edges where the endNode1 or endNode2 is in the visitedNodes set
        for (Edge edge : simplifiedEdges.values()) {
            if (visitedNodes.contains(edge.endNode1.id) || visitedNodes.contains(edge.endNode2.id)) {
                System.out.println(edge);
            }
        }

        List<Node> nodesToRemove = new ArrayList<>();
        for (Node node : nodes.values()) {
            if (visitedNodes.contains(node.id)) {
                nodesToRemove.add(node);
            }
        }
        for (Node node : nodesToRemove) {
            nodes.remove(node.id);
        }

        System.out.println("Number of edges after simplification: " + simplifiedEdges.size());
        System.out.println("Number of nodes after simplification: " + nodes.size());

        this.edges = simplifiedEdges;

        String file = "bretigny_62p_1147n_1235e.json";
        OutputWriter writer = new OutputWriter(new Graph(this.nodes, this.edges));
        writer.write("output/output_" + file, 10.0);
        // exit program
        System.exit(0);
    }

    public void shave(){

    }
}
