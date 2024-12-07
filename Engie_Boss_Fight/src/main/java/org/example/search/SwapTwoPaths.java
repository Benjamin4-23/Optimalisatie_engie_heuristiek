package org.example.search;

import org.example.domain.*;
import org.example.search.framework.Move;
import org.example.search.framework.Solution;

import java.util.*;

public class SwapTwoPaths extends Move{
    private Solution solution;
    private HashMap<Integer, Edge> oldEdges = new HashMap<>();
    private  Graph graph;
    private double delta;
    private double disgardedCost = 0.0;
    private double oldCost = 0.0;
    List<Integer> indexes = new ArrayList<>();
    Set<Integer> disgardedEdges = new HashSet<>();
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

        // Get 5 random edges from graph.unlockedEdges
        for (int i = 0; i < 10; i++) {
            int counter = 0;
            int randomIndex = 0;
            do{
                randomIndex = (int) (Math.random() * this.graph.unlockedEdges.size());
                counter++;
            }while(indexes.contains(randomIndex) ||
                    !this.graph.edges.get(this.graph.unlockedEdges.get(randomIndex)).isUsed ||
                    this.graph.edges.get(this.graph.unlockedEdges.get(randomIndex)).isLocked ||
                    counter < 20);
            this.indexes.add(this.graph.unlockedEdges.get(randomIndex));
        }

        for (Integer index : indexes) {
            Edge edge = this.graph.edges.get(index);
            this.disgardedEdges.add(edge.id);
            this.disgardedCost += edge.cost;
            this.graph.usedEdges.remove(edge.id);
            edge.disgard();
            setReconnect(edge);
        }

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
        double cost = this.graph.reconnect(); // Cost to replace deleted edges
        System.out.printf("cost: %.4f%n",cost);
        //double newCost = calculateEvaluation();
        double newCost = this.oldCost - this.disgardedCost + cost;
        this.delta = newCost - oldCost;
        System.out.printf("old cost: %.4f, new cost: %.4f%n", oldCost, newCost);
        //this.delta = cost - this.disgardedCost;
        double currentResult = solution.getObjectiveValue();
        double deltaEvaluation = currentResult + this.delta;
        solution.setObjectiveValue(deltaEvaluation);
        return deltaEvaluation;
    }

    private void setReconnect(Edge edge){
        this.blockedEdges.add(edge);
        edge.isBlocked = true;
        this.graph.reconnects.add(new ReconnectPair(edge));
        return;
        //TODO: merge reconnects if possible
        // in 1 lijn allemaal met elkaar verbinden
        // bij splitsing op zelfde node in 2

        /*int BlockCount1 = getBlockCount(edge.endNode1);
        int BlockCount2 = getBlockCount(edge.endNode2);
        if(BlockCount1 == 1 || BlockCount2 == 1){
            if(BlockCount1==1){
                // get reconnect pair
            }
        }*/
    }

    private int getBlockCount(Node node){
        int count = 0;
        for (Edge edge : node.edges.values()){
            if(edge.isBlocked) count++;
        }
        return count;
    }
}
