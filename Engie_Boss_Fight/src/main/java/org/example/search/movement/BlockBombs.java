package org.example.search.movement;

import org.example.domain.Edge;
import org.example.domain.EdgeType;
import org.example.domain.Graph;
import org.example.search.MySolution;
import org.example.search.framework.Move;
import org.example.search.framework.Solution;

import java.util.*;

public class BlockBombs extends Move{
    private Solution solution;
    private  Graph graph;
    private double delta;
    private double disgardedCost = 0.0;
    private double oldCost = 0.0;

    private HashMap<Integer, Edge> oldEdges = new HashMap<>();
    List<Integer> indexes = new ArrayList<>();
    Set<Edge> blockedEdges = new HashSet<>();

    @Override
    public double doMove(Solution solution) {
        this.disgardedCost = 0.0;
        this.delta = 0.0;
        this.indexes.clear();
        this.solution = solution;
        this.blockedEdges.clear();

        this.oldCost = solution.getObjectiveValue();
        this.graph = ((MySolution) solution).getGraph();
        this.oldEdges = new HashMap<>(graph.usedEdges);

        //List<Edge> temp = new ArrayList<>(this.graph.unlockedEdges);
        // Get 5 random edges from graph.unlockedEdges
        for (int i = 0; i < 25; i++) {
            int counter = 0;
            int randomIndex = 0;
            do{
                randomIndex = (int) (Math.random() * this.graph.unlockedEdges.size());

                counter++;
            }while(indexes.contains(randomIndex) ||
                    !this.graph.edges.get(this.graph.unlockedEdges.get(randomIndex)).isUsed ||
                    this.graph.edges.get(this.graph.unlockedEdges.get(randomIndex)).isLocked ||
                    counter < 30);
            this.indexes.add(this.graph.unlockedEdges.get(randomIndex));
        }

        for (Integer index : indexes) {
            Edge edge = this.graph.edges.get(index);
            this.disgardedCost += edge.cost;
            this.graph.usedEdges.remove(edge.id);
            edge.disgard();
            //blockedEdges.add(edge);
            //edge.isBlocked = true;
        }
        //return 0.0;
        return calculateDeltaEvaluation();
    }

    @Override
    public void undoMove(Solution solution) {
        this.graph = ((MySolution) solution).getGraph();
        for(Edge edge : this.graph.edges.values()){
            edge.disgard();
        }
        this.graph.usedEdges.clear();
        for (Edge edge : this.oldEdges.values()) {
            this.graph.edges.get(edge.id).use();
            this.graph.usedEdges.put(edge.id, edge);
        }

        for(Edge edge: blockedEdges){
            edge.isBlocked = false;
        }
        this.blockedEdges.clear();
        solution.setObjectiveValue(oldCost);
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
        // Cost to reconnect the graph
        double cost = this.graph.dijkstra();
        double newCost = calculateEvaluation();
        solution.setObjectiveValue(newCost);
        return newCost;
        //double newCost = this.oldCost - this.disgardedCost + cost;
        //System.out.printf("cost: %.4f%n",cost);
        //return cost;
        //double newCost = calculateEvaluation();
        /*double newCost = this.oldCost - this.disgardedCost + costthis.disgardedCost + cost;
        this.delta = newCost - oldCost;
        System.out.printf("old cost: %.4f, new cost: %.4f%n", oldCost, newCost);
        //this.delta = cost - this.disgardedCost;
        double currentResult = solution.getObjectiveValue();
        double deltaEvaluation = currentResult + this.delta;
        solution.setObjectiveValue(deltaEvaluation);
        return deltaEvaluation;*/
    }


}
