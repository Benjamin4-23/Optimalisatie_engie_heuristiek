package org.example.search;

import org.example.data.OutputWriter;
import org.example.search.framework.RandomGenerator;
import org.example.search.framework.SearchAlgorithm;
import org.example.search.hillclimbing.SteepestDescent;

public class Main {
    // Set max runtime as 10 minutes
    public static final long MAX_RUNTIME = 10 * 60 * 1000;
    public static void main(String[] args) {
        if (args.length == 2) {
            // via validator
            String path = args[0];
            RandomGenerator randomGenerator = new RandomGenerator(1); // Use seed 1 for validation
            SearchAlgorithm alg = new SteepestDescent(path, randomGenerator);
            alg.execute(20000);
            MySolution bestSolution = (MySolution) alg.getBestSolution();
            OutputWriter writer = new OutputWriter(bestSolution.getGraph(), bestSolution.getObjectiveValue());
            writer.write(args[1]);
            return;
        }

        // normal run with parallel execution
        String file = "bretigny_62p_1147n_1235e.json"; // example file
        
        // Create multiple threads with different random seeds
        int numThreads = 10;
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            final int seed = i;
            threads[i] = new Thread(() -> {
                RandomGenerator randomGenerator = new RandomGenerator(seed);
                SearchAlgorithm alg = new SteepestDescent("data/" + file, randomGenerator);
                
                // Start timer
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < MAX_RUNTIME) {
                    alg.execute(10);
                }
                
                MySolution bestSolution = (MySolution) alg.getBestSolution();
                System.out.println("Thread " + seed + " best solution: " + bestSolution.getObjectiveValue());
                
                OutputWriter writer = new OutputWriter(bestSolution.getGraph(), bestSolution.getObjectiveValue());
                writer.write("output/output_" + seed + "_" + file);
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}