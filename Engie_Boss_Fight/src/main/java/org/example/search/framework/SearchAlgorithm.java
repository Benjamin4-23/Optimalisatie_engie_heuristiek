package org.example.search.framework;

public abstract class SearchAlgorithm {

    public abstract Solution getBestSolution();

    public abstract Solution getCurrentSolution();

    public abstract double execute(int numberOfIterations) ;

}
