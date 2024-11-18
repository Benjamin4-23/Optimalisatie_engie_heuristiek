package org.example.search;

import org.example.search.framework.Move;
import org.example.search.framework.Solution;

public class MySwapTwoRandomPaths extends Move{
    private Solution solution;
    private double delta;
    private double oldCost = 0.0;
    @Override
    public double doMove(Solution solution) {
        this.solution = solution;
        oldCost = solution.getObjectiveValue();

        // switch two random paths
        // ...


        return calculateDeltaEvaluation();
    }

    @Override
    public void undoMove(Solution solution) {
        // switch back to the original paths
        // ...
        double currentCost = solution.getObjectiveValue();
        solution.setObjectiveValue(currentCost - delta);
    }

    private double calculateEvaluation(){
        return 0.0;
    }
    private double calculateDeltaEvaluation() {

        return 0.0;
    }
}
