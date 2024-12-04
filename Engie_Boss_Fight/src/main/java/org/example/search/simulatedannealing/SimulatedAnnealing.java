package org.example.search.simulatedannealing;


import org.example.search.MyObjectiveFunction;
import org.example.search.MySolution;
import org.example.search.SwapTwoPaths;
import org.example.search.framework.*;

public class SimulatedAnnealing extends SearchAlgorithm {
    private ObjectiveFunction function;
    private Solution currentSolution;
    private Solution bestSolution;
    private double currentResult;
    private double bestResult;

    public SimulatedAnnealing(String path) {
        this.function = new MyObjectiveFunction();
        this.currentSolution = new MySolution(path);
        bestSolution = currentSolution;
        bestResult = function.evaluate(bestSolution,null);
    }

    @Override
    public double execute(int numberOfIterations) {
        currentResult = bestResult;
        Move move = new SwapTwoPaths();
        for (int i = 0; i <= numberOfIterations; i++) {
            if (currentResult < bestResult) {
                bestResult = currentResult;
                bestSolution = (MySolution)currentSolution.clone();
                System.out.println(bestResult);

            } else if (Math.exp((bestResult - currentResult) / Math.sqrt(currentResult) / (1.0001 - ((i * 1.0) / numberOfIterations))) < RandomGenerator.random.nextDouble()) {
                move.undoMove(currentSolution);
            }
            currentResult = function.evaluate(currentSolution, move);
            //System.out.println(currentResult);
        }
        //System.out.println("bestSolution " + function.evaluate(bestSolution,null) + " " + bestResult);
        return bestResult;
    }

    @Override
    public Solution getBestSolution() {
        return bestSolution;
    }

    @Override
    public Solution getCurrentSolution() {
        return currentSolution;
    }
}
