package org.example.search;

import org.example.data.OutputWriter;
import org.example.search.framework.SearchAlgorithm;
import org.example.search.hillclimbing.SteepestDescent;
import org.example.search.lateacceptance.LateAcceptanceStrategy;
import org.example.search.simulatedannealing.SimulatedAnnealing;

public class Main {
    public static void main(String[] args) {
        if (args.length == 2) {
            // via validator
            String path = args[0];
            SearchAlgorithm alg = new SteepestDescent(path);
            alg.execute(1000);
            MySolution bestSolution = (MySolution) alg.getBestSolution();
            OutputWriter writer = new OutputWriter(bestSolution.getGraph(), bestSolution.getObjectiveValue());
            writer.write(args[1]);
            return;
        }

        //normal run
        //generate normal dijkstra file
        String file = "bretigny_62p_1147n_1235e.json";
        SearchAlgorithm alg = new SteepestDescent("data/" + file);
        alg.execute(1);
        MySolution bestSolution = (MySolution) alg.getBestSolution();
        OutputWriter writer = new OutputWriter(bestSolution.getGraph(), bestSolution.getObjectiveValue());
        writer.write("output/output_" + file);
    }
}