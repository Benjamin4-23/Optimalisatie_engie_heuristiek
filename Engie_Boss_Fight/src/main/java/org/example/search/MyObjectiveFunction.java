package org.example.search;


import org.example.domain.Graph;
import org.example.search.framework.Move;
import org.example.search.framework.ObjectiveFunction;
import org.example.search.framework.Solution;

public class MyObjectiveFunction extends ObjectiveFunction {

    @Override
    public double evaluate(Solution solution, Move move) {
        return 0;
    }
    private double absoluteEvaluation(Solution solution) {
        double cost = 0.0;
        Graph graph = ((MySolution) solution).getGraph();

        return cost;
    }
    private double deltaEvaluation(Solution solution, Move move) {
        return move.doMove(solution);
    }
}
