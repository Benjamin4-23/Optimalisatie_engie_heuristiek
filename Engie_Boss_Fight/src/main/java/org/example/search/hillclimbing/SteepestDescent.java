package org.example.search.hillclimbing;

import org.example.search.MyObjectiveFunction;
import org.example.search.MySolution;
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
    }
    @Override
    public double execute(int numberOfIterations) {
        return 0;
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
