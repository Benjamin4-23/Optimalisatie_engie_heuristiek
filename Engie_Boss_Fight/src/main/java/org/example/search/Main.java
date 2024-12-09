package org.example.search;

import org.example.data.OutputWriter;
import org.example.search.framework.SearchAlgorithm;
import org.example.search.hillclimbing.SteepestDescent;
import org.example.search.lateacceptance.LateAcceptanceStrategy;
import org.example.search.simulatedannealing.SimulatedAnnealing;

public class Main {
    // Set max runtime as 10 minutes
    public static final long MAX_RUNTIME = 10 * 60 * 1000;
    public static void main(String[] args) {
        if (args.length == 2) {
            // via validator
            String path = args[0];
            SearchAlgorithm alg = new SteepestDescent(path) ;
            alg.execute(20000);
            MySolution bestSolution = (MySolution) alg.getBestSolution();
            OutputWriter writer = new OutputWriter(bestSolution.getGraph(), bestSolution.getObjectiveValue());
            writer.write(args[1]);
            return;
        }

        //normal run
        //generate normal dijkstra file
        String file = "";
        int fileNumber = 2;
        switch (fileNumber){
            case 1: file = "bretigny_62p_1147n_1235e.json"; break; // STDE @ 22, 20
            case 2: file = "bagnolet_353p_3844n_4221e.json"; break; // STDE @ 40, 20 kan evt ook? 100 lukt ook!
            case 3: file = "bretigny_576n_9591n_10353e.json"; break; // STDE @ 100
            case 4: file = "bagnolet_1366p_13523n_15065e.json"; break; // STDE @ 100
            case 5: file = "bagnolet_2081p_18464n_20478e.json"; break; // STDE @ 100
        }

        SearchAlgorithm alg = new SteepestDescent("data/" + file);
        // Start timer
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < MAX_RUNTIME) {
            alg.execute(10);
        }
        MySolution bestSolution = (MySolution) alg.getBestSolution();
        System.out.println("Best solution: " + bestSolution.getObjectiveValue());
        OutputWriter writer = new OutputWriter(bestSolution.getGraph(), bestSolution.getObjectiveValue());
        writer.write("output/output_" + file);
    }
}