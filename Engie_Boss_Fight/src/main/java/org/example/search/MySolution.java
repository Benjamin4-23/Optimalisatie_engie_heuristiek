package org.example.search;

import org.example.domain.Graph;
import org.example.search.framework.Solution;

public class MySolution implements Solution {
    private Graph graph;
    private double cost;
    public MySolution() {
        // this.graph = datareader bla bla
        // GenerateSolution();
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
    }

}
