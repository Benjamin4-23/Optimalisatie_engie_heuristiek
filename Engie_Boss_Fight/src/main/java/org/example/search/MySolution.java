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
        this.graph = new Graph(reader.getNodes(), reader.getEdges());
        graph.transform();
        graph.simplify();
        graph.shave();
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
        graph.dijkstra();

    }
    public Graph getGraph() {
        return graph;
    }

}
