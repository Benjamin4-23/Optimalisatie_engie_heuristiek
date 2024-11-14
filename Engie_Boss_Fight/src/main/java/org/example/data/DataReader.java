package org.example.data;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.example.domain.Edge;
import org.example.domain.Node;
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
                nodes.put(id, new Node(id, x, y, Node.NodeType.valueOf(type.toUpperCase())));
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

                edges.put(id, new Edge(id, Edge.EdgeType.valueOf(type.toUpperCase()), cost, endNode1, endNode2));
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
            if(edge.edgeType == Edge.EdgeType.EXISTING){
                existingNodes.add(edge.endNode1);
                existingNodes.add(edge.endNode2);
            }
            if (nodes.get(edge.endNode1.id).nodeType == Node.NodeType.PROSPECT ||
                    nodes.get(edge.endNode2.id).nodeType == Node.NodeType.PROSPECT) {
                // For edges involving a prospect, add a single directed edge towards the prospect
                if (nodes.get(edge.endNode1.id).nodeType == Node.NodeType.PROSPECT) {
                    directedEdges.add(new Edge(i++, edge.edgeType, edge.cost, edge.endNode2, edge.endNode1));
                } else {
                    directedEdges.add(new Edge(i++, edge.edgeType, edge.cost, edge.endNode1, edge.endNode2));
                }
            } else {
                // For regular nodes, add two directed edges
                directedEdges.add(new Edge(i++, edge.edgeType, edge.cost, edge.endNode1, edge.endNode2));
                directedEdges.add(new Edge(i++, edge.edgeType, edge.cost, edge.endNode2, edge.endNode1));
            }
        }
        edges.clear();
        for (Edge edge : directedEdges) {
            edges.put(edge.id, edge);
        }

        // Step 2: Add a virtual root node
        int rootId = -1;
        rootNode = new Node(rootId, 0.0, 0.0, Node.NodeType.REGULAR); // ID -1 for the virtual root
        nodes.put(rootId, rootNode);

        // Connect the root node to all existing non-prospect nodes in the network with cost 0
        for (Node node : existingNodes) {
            if (node.nodeType != Node.NodeType.PROSPECT) {
                Edge edgeFromRoot = new Edge(i++, Edge.EdgeType.EXISTING, 0, rootNode, node);
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
}
