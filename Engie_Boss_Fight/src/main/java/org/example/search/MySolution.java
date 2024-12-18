package org.example.search;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.example.data.DataReader;
import org.example.data.OutputWriter;
import org.example.domain.Graph;
import org.example.domain.Node;
import org.example.search.framework.Solution;

public class MySolution implements Solution {
    private Graph graph;
    private double cost;
    public DataReader reader;
    public MySolution(String path) {
        assert Files.exists(Path.of(path));
        reader = new DataReader(path);
        reader.loadData();
        reader.transform();
        reader.shave();
        reader.simplify();
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
        MySolution copy = null;
        try {
            copy = new MySolution(new Graph(this.graph), this.cost);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return copy;
    }

    public void GenerateSolution() {
        // Generate a random solution
        // We will start on the existing network, connect all prospects
        // We'll use dijkstra to find the shortest path between all prospects
        graph.dijkstraFromRootToProspects();

    }
    public Graph getGraph() {
        return graph;
    }

}
