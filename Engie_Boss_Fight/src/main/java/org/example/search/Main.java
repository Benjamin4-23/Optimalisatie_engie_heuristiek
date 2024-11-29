package org.example.search;

import org.example.data.OutputWriter;
import org.example.search.framework.SearchAlgorithm;
import org.example.search.hillclimbing.SteepestDescent;

public class Main {
    public static void main(String[] args) {
        if (args.length == 2) {
            // via validator
            String path = args[0];
            SearchAlgorithm steepestDescent = new SteepestDescent(path);
            //steepestDescent.execute(150);
            MySolution bestSolution = (MySolution) steepestDescent.getBestSolution();
            OutputWriter writer = new OutputWriter(bestSolution.getGraph());
            writer.write(args[1], bestSolution.getObjectiveValue());
            return;
        }

        //normal run
        String file = "bretigny_62p_1147n_1235e.json";
        SearchAlgorithm steepestDescent = new SteepestDescent("data/" + file);
        //steepestDescent.execute(150);
        MySolution bestSolution = (MySolution) steepestDescent.getBestSolution();
        OutputWriter writer = new OutputWriter(bestSolution.getGraph());
        writer.write("output/output_" + file, bestSolution.getObjectiveValue());
    }
}