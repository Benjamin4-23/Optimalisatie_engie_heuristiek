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


}
