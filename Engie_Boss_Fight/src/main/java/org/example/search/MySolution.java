package org.example.search;

import java.util.Map;

import org.example.data.DataReader;
import org.example.data.OutputWriter;
import org.example.domain.Graph;
import org.example.domain.Node;
import org.example.search.framework.Solution;

public class MySolution implements Solution {
    private Graph graph;
    private double cost;
    public MySolution() {
        DataReader reader = new DataReader("data/bretigny_62p_1147n_1235e.json");
        reader.loadData();
        reader.transform();
        this.graph = new Graph(reader.getNodes(), reader.getEdges());
        GenerateSolution();
        OutputWriter writer = new OutputWriter(this.graph);
        writer.write("graph_data.json");

    }
    public MySolution(Graph g, double d){
        this.graph = g;
        this.cost = d;
    }
    @Override
    public double getObjectiveValue() {
        return this.cost;
    }

    @Override
    public void setObjectiveValue(double value) {
        this.cost = value;
    }

    @Override
    public Object clone() { // TODO: Make a deep copy of the solution

        return null;
    }

    public void GenerateSolution() {
        // Generate a random solution
        // We will start on the existing network, connect all prospects
        // We'll use dijkstra to find the shortest path between all prospects
        Map<Node, Double> test = graph.dijkstraFromRootToProspects();

    }
    public Graph getGraph() {
        return graph;
    }

}
