package org.example.search;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.example.data.OutputWriter;
import org.example.search.framework.RandomGenerator;
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
            RandomGenerator randomGenerator = new RandomGenerator(1); // Use seed 1 for validation
            SearchAlgorithm alg = new SteepestDescent(path, randomGenerator);
            alg.execute(20000);
            MySolution bestSolution = (MySolution) alg.getBestSolution();
            OutputWriter writer = new OutputWriter(bestSolution.getGraph(), bestSolution.getObjectiveValue());
            writer.write(args[1]);
            return;
        }

        //normal run
        /*
            "bretigny_62p_1147n_1235e.json" // STDE @ 22, 20
            "bagnolet_353p_3844n_4221e.json" // STDE @ 40, 20 kan evt ook? 100 lukt ook!
            "bretigny_576n_9591n_10353e.json" // STDE @ 100
            "bagnolet_1366p_13523n_15065e.json" // STDE @ 100
            "bagnolet_2081p_18464n_20478e.json" // STDE @ 100
        */
        final String file = "bagnolet_1366p_13523n_15065e.json";

        int numThreads = 10;

        // Create thread pool and future list to store results
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        Future<Result>[] futures = new Future[numThreads];

        // Start parallel executions
        for (int i = 0; i < numThreads; i++) {
            final int seed = i + 1;
            futures[i] = executor.submit(() -> {
                RandomGenerator randomGenerator = new RandomGenerator(seed);
                SearchAlgorithm alg = new SteepestDescent("data/" + file, randomGenerator);
                
                // Start timer
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < MAX_RUNTIME) {
                    alg.execute(10);
                }
                
                MySolution solution = (MySolution) alg.getBestSolution();
                return new Result(seed, solution, solution.getObjectiveValue());
            });
        }

        // Find best result and store all results
        Result bestResult = null;
        Result[] allResults = new Result[numThreads];
        
        for (int i = 0; i < numThreads; i++) {
            try {
                Result result = futures[i].get();
                allResults[i] = result;
                if (bestResult == null || result.cost < bestResult.cost) {
                    bestResult = result;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Shutdown executor
        executor.shutdown();

        // Print all results
        System.out.println("\nResults from all threads:");
        System.out.println("------------------------");
        for (Result result : allResults) {
            System.out.printf("Thread %d (seed: %d) - Cost: %.2f%n", 
                            result.seed, result.seed, result.cost);
        }

        // Print and save best result
        if (bestResult != null) {
            System.out.println("\nBEST RESULT:");
            System.out.println("------------------------");
            System.out.println("Thread " + bestResult.seed + 
                             " (seed: " + bestResult.seed + 
                             ") with cost: " + bestResult.cost);
            OutputWriter writer = new OutputWriter(bestResult.solution.getGraph(), 
                                                 bestResult.solution.getObjectiveValue());
            writer.write("output/output_" + file);
        }
    }

    // Helper class to store results
    private static class Result {
        final int seed;
        final MySolution solution;
        final double cost;

        Result(int seed, MySolution solution, double cost) {
            this.seed = seed;
            this.solution = solution;
            this.cost = cost;
        }
    }
}