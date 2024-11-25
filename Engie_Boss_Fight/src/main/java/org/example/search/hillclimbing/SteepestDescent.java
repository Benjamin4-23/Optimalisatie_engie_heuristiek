package org.example.search.hillclimbing;

import org.example.search.MyObjectiveFunction;
import org.example.search.MySolution;
import org.example.search.SwapTwoPaths;
import org.example.search.framework.Move;
import org.example.search.framework.SearchAlgorithm;
import org.example.search.framework.Solution;

public class SteepestDescent extends SearchAlgorithm {
    private MyObjectiveFunction function;
    private Solution currentSolution;
    private Solution bestSolution;
    private double currentResult;
    private double bestResult;

    public SteepestDescent() {
        this.function = new MyObjectiveFunction();
        this.currentSolution = new MySolution();
        this.bestSolution = this.currentSolution;
        this.bestResult = this.function.evaluate(this.bestSolution, null);
        System.out.println("bestResult " + bestResult);
    }

    public SteepestDescent(String file) {
        this.function = new MyObjectiveFunction();
        this.currentSolution = new MySolution(file);
        this.bestSolution = this.currentSolution;
        this.bestResult = this.function.evaluate(this.bestSolution, null);
        System.out.println("bestResult " + bestResult);
    }
    @Override
    public double execute(int numberOfIterations) {
        currentResult = bestResult;
        Move move = new SwapTwoPaths();
        for (int i = 0; i < numberOfIterations; i++) {
            if (currentResult <= bestResult) {
                bestResult = currentResult;
                bestSolution = (MySolution) currentSolution.clone();
                //System.out.println(bestResult);
            }
            else {
                move.undoMove(currentSolution);
            }
            currentResult =  function.evaluate(currentSolution, move);

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
