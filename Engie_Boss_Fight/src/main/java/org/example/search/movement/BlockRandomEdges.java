package org.example.search.movement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.example.domain.Edge;
import org.example.domain.EdgeType;
import org.example.domain.Graph;
import org.example.search.MySolution;
import org.example.search.framework.Move;
import org.example.search.framework.RandomGenerator;
import org.example.search.framework.Solution;

public class BlockRandomEdges extends Move{
    private final Random random;
    private Solution solution;
    private  Graph graph;
    private double delta;
    private double disgardedCost = 0.0;
    private double oldCost = 0.0;

    private HashMap<Integer, Edge> oldEdges = new HashMap<>();
    List<Integer> indexes = new ArrayList<>();
    Set<Edge> blockedEdges = new HashSet<>();

    private int numberOfEdges = 100; // best is 25 with math.random(), 22 with the random

    public BlockRandomEdges(RandomGenerator randomGenerator) {
        this.random = randomGenerator.getRandom();
    }

    @Override
    public double doMove(Solution solution) {
        this.disgardedCost = 0.0;
        this.delta = 0.0;
        this.indexes.clear();
        this.solution = solution;
        this.blockedEdges.clear();

        this.oldCost = solution.getObjectiveValue();
        this.graph = ((MySolution) solution).getGraph();
        this.numberOfEdges = this.graph.unlockedEdges.size() > 1000? 100 : 25;
        this.oldEdges = new HashMap<>(graph.usedEdges);

        this.indexes = selectTerminalEdges(numberOfEdges);

        for (Integer index : indexes) {
            Edge edge = this.graph.edges.get(index);
            this.disgardedCost += edge.cost;
            this.graph.usedEdges.remove(edge.id);
            edge.disgard();
        }
        //return 0.0;
        return calculateDeltaEvaluation();
    }

    private List<Integer> selectTerminalEdges(int numberOfEdges){
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < numberOfEdges; i++) {
            int counter = 0;
            int randomIndex;
            do{
                //randomIndex = (int) (this.random.nextInt(this.graph.unlockedEdges.size()));
                randomIndex = (this.random.nextInt(this.graph.unlockedEdges.size()));
                counter++;
            }while(idx.contains(randomIndex) ||
                    !this.graph.edges.get(this.graph.unlockedEdges.get(randomIndex)).isUsed ||
                    this.graph.edges.get(this.graph.unlockedEdges.get(randomIndex)).isLocked ||
                    counter < 30);
            idx.add(this.graph.unlockedEdges.get(randomIndex));
        }
        return idx;
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

    }


}
