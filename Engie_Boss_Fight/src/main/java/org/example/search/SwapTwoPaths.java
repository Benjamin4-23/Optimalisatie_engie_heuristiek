package org.example.search;

import org.example.domain.Edge;
import org.example.domain.EdgeType;
import org.example.domain.Graph;
import org.example.search.framework.Move;
import org.example.search.framework.Solution;

import java.util.ArrayList;
import java.util.List;

public class SwapTwoPaths extends Move{
    private Solution solution;
    private  Graph graph;
    private double delta;
    private double oldCost = 0.0;
    List<Integer> indexes = new ArrayList<>();

    @Override
    public double doMove(Solution solution) {
        this.solution = solution;
        oldCost = solution.getObjectiveValue();

        // Select subset of paths
        graph = ((MySolution) solution).getGraph();

        for (int i = 0; i < 5; i++) {
            // Get 5 random edges from graph.unlockedEdges
            int randomIndex = 0;
            do{
                randomIndex = (int) (Math.random() * graph.unlockedEdges.size());
            }while(indexes.contains(randomIndex));
            indexes.add(this.graph.unlockedEdges.get(randomIndex));
        }

        for (Integer index : indexes) {
            Edge edge = graph.edges.get(index);
            this.oldCost += edge.cost;
            edge.disgard();
        }


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
        this.graph.dijkstra();
        double newCost = calculateEvaluation();
        this.delta = newCost - oldCost;
        double currentResult = solution.getObjectiveValue();
        double deltaEvaluation = currentResult + this.delta;
        solution.setObjectiveValue(deltaEvaluation);
        return deltaEvaluation;
    }
}
