package org.example.search;

import org.example.data.DataReader;
import org.example.domain.Graph;
import org.example.search.framework.Solution;

public class MySolution implements Solution {
    private Graph graph;
    private double cost;
    public MySolution() {
        DataReader reader = new DataReader("Engie_Boss_Fight/data/bagnolet_353p_3844n_4221e.json");
        reader.loadData();
        reader.transform();
        this.graph = new Graph(reader.getNodes(), reader.getEdges());
        GenerateSolution();
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
    public Object clone() {
        return null;
    }

    public void GenerateSolution() {
        // Generate a random solution
        // We will start on the existing network, connect all prospects
        // We'll use dijkstra to find the shortest path between all prospects

    }
    public Graph getGraph() {
        return graph;
    }

}
