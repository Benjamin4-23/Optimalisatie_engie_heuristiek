package org.example.search.hillclimbing;

import org.example.search.MyObjectiveFunction;
import org.example.search.MySolution;
import org.example.search.movement.BlockBombing;
import org.example.search.movement.BlockRandomEdges;
import org.example.search.framework.Move;
import org.example.search.framework.SearchAlgorithm;
import org.example.search.framework.Solution;
import org.example.search.framework.RandomGenerator;

public class SteepestDescent extends SearchAlgorithm {
    private final RandomGenerator randomGenerator;
    private MyObjectiveFunction function;
    private MySolution currentSolution;
    private MySolution bestSolution;
    private double currentResult;
    private double bestResult;

    public SteepestDescent(String path, RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
        this.function = new MyObjectiveFunction();
        this.currentSolution = new MySolution(path);
        this.bestSolution = this.currentSolution;
        this.bestResult = this.currentSolution.getObjectiveValue();
        System.out.println("Initial result: " + bestResult);
    }
    @Override
    public double execute(int numberOfIterations) {
        currentResult = bestResult;
        Move move = new BlockRandomEdges(randomGenerator);
        for (int i = 0; i < numberOfIterations; i++) {
            currentResult = function.evaluate(currentSolution, move);
            if (currentResult <= bestResult) {
                bestResult = currentResult;
                System.out.println(/*"New best result: " + */bestResult);
                bestSolution = new MySolution(currentSolution);
                bestSolution.setObjectiveValue(function.evaluate(bestSolution, null));
            }
            else {
                //System.out.println("Undoing move");
                move.undoMove(currentSolution);
            }
            //System.out.println("New best solution: " + bestSolution.getObjectiveValue());

        }
        //System.out.println("bestSolution " + function.evaluate(bestSolution, null) + " " + bestResult);
        return bestResult;
    }
    @Override
    public Solution getBestSolution() {
        return this.bestSolution;
    }

    @Override
    public Solution getCurrentSolution() {
        return this.currentSolution;
    }


}
