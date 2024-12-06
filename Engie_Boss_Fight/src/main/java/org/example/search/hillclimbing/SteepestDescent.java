package org.example.search.hillclimbing;

import org.example.search.MyObjectiveFunction;
import org.example.search.MySolution;
import org.example.search.SwapTwoPaths;
import org.example.search.framework.Move;
import org.example.search.framework.SearchAlgorithm;
import org.example.search.framework.Solution;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class SteepestDescent extends SearchAlgorithm {
    private MyObjectiveFunction function;
    private Solution currentSolution;
    private Solution bestSolution;
    private double currentResult;
    private double bestResult;

    public SteepestDescent(String path) {
        this.function = new MyObjectiveFunction();
        this.currentSolution = new MySolution(path);
        this.bestSolution = this.currentSolution;
        //this.bestResult = this.function.evaluate(this.bestSolution, null);
        this.bestResult = this.currentSolution.getObjectiveValue();
        System.out.println("Initial result: " + bestResult);
    }
    @Override
    public double execute(int numberOfIterations) {
        currentResult = bestResult;
        Move move = new SwapTwoPaths();
        for (int i = 0; i < numberOfIterations; i++) {
            currentResult =  function.evaluate(currentSolution, move);
            if (currentResult <= bestResult) {
                bestResult = currentResult;
                bestSolution = (MySolution) currentSolution.clone();
                System.out.println(bestResult);
            }
            else {
                //System.out.println("Undoing move");
                move.undoMove(currentSolution);
            }

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
