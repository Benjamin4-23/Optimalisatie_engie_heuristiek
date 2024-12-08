package org.example.search.simulatedannealing;


import org.example.search.MyObjectiveFunction;
import org.example.search.MySolution;
import org.example.search.movement.BlockRandomEdges;
import org.example.search.framework.*;

public class SimulatedAnnealing extends SearchAlgorithm {
    private ObjectiveFunction function;
    private MySolution currentSolution;
    private MySolution bestSolution;
    private double currentResult;
    private double bestResult;

    public SimulatedAnnealing(String path) {
        this.function = new MyObjectiveFunction();
        this.currentSolution = new MySolution(path);
        bestSolution = currentSolution;
        bestResult = function.evaluate(bestSolution,null);
        System.out.println("Initial result: " + bestResult);
    }

    @Override
    public double execute(int numberOfIterations) {
        currentResult = bestResult;
        for (int i = 0; i <= numberOfIterations; i++) {
            Move move = new BlockRandomEdges();

            if (currentResult < bestResult) {
                bestResult = currentResult;
                System.out.println("New best result: " + bestResult);
                bestSolution = new MySolution(currentSolution);
                bestSolution.setObjectiveValue(function.evaluate(bestSolution, null));

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
