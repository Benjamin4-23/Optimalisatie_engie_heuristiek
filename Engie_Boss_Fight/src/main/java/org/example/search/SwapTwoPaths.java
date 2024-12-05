package org.example.search;

import org.example.domain.Edge;
import org.example.domain.EdgeType;
import org.example.domain.Graph;
import org.example.search.framework.Move;
import org.example.search.framework.Solution;

public class SwapTwoPaths extends Move{
    private Solution solution;
    private double delta;
    private double oldCost = 0.0;
    @Override
    public double doMove(Solution solution) {
        this.solution = solution;
        oldCost = solution.getObjectiveValue();
        // TODO: switch two random paths


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
        double cost = 0.0;
        Graph graph = ((MySolution) solution).getGraph();

        for (Edge edge : graph.edges.values()) {
            if(edge.isUsed){
                if(edge.edgeType != EdgeType.EXISTING){
                    cost += edge.cost;
                }
            }
        }
        return cost;
    }
    private double calculateDeltaEvaluation() {
        // TODO: calculate the delta evaluation
        return 0.0;
    }
}
