package org.example.search;

import org.example.data.DataReader;
import org.example.data.OutputWriter;
import org.example.search.framework.SearchAlgorithm;
import org.example.search.hillclimbing.SteepestDescent;

public class Main {
    public static void main(String[] args) {
        System.out.println(args);
        SearchAlgorithm steepestDescent = new SteepestDescent();
        //steepestDescent.execute(150);
        MySolution bestSolution = (MySolution) steepestDescent.getBestSolution();
        OutputWriter writer = new OutputWriter(bestSolution.getGraph());
        writer.write("output/graph_data.json", bestSolution.getObjectiveValue());
    }
}