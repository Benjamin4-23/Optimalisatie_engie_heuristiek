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
import org.example.domain.Node;
import org.example.search.MySolution;
import org.example.search.framework.Move;
import org.example.search.framework.RandomGenerator;
import org.example.search.framework.Solution;

public class BlockBombing extends Move{
    private final Random random;
    private Solution solution;
    private  Graph graph;
    private double delta;
    private double disgardedCost = 0.0;
    private double oldCost = 0.0;

    private HashMap<Integer, Edge> oldEdges = new HashMap<>();
    List<Integer> indexes = new ArrayList<>();
    Set<Edge> blockedEdges = new HashSet<>();

    private int numberOfBombs = 5;
    private int bombRadius = 5;

    public BlockBombing(RandomGenerator randomGenerator) {
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
        this.oldEdges = new HashMap<>(graph.usedEdges);

        this.indexes = bombSolution(numberOfBombs, bombRadius);

        for (Integer index : indexes) {
            Edge edge = this.graph.edges.get(index);
            this.disgardedCost += edge.cost;
            this.graph.usedEdges.remove(edge.id);
            edge.disgard();
        }
        //return 0.0;
        return calculateDeltaEvaluation();
    }

    private List<Integer> bombSolution(int numberOfBombs, int bombRadius){
        List<Integer> edgeIndexes = new ArrayList<>();

        Set<Integer> visitedNodes = new HashSet<>();
        Set<Edge> edgesToRemove = new HashSet<>();

        // Select random targets
        List<Integer> usedNodes = new ArrayList<>(this.graph.usedNodes);

        List<Integer> targets = new ArrayList<>();
        for (int i = 0; i < numberOfBombs; i++) {
            int counter = 0;
            int randomIndex;
            Integer idx;
            Node node;
            do {
                randomIndex = (this.random.nextInt(usedNodes.size()));
                counter++;
                idx = usedNodes.get(randomIndex);
                node = this.graph.nodes.get(idx);
            } while (targets.contains(idx) || // Check for node ID, not index
                    node.isLocked ||
                    counter < 30);
            targets.add(idx); // Add the node ID
        }

        // Bomb targets
        for (Integer target : targets) {
            // get the target node, and delete edges in all directions for radius nodes
            edgeIndexes.addAll(bombNode(target, bombRadius, edgesToRemove, visitedNodes));
        }
        return edgeIndexes;
    }

    private List<Integer> bombNode(int targetNodeId, int radius, Set<Edge> edgesToRemove, Set<Integer> visitedNodes) {

        if (radius < 0 || visitedNodes.contains(targetNodeId)) {
            return new ArrayList<>();
        }
        List<Integer> idx = new ArrayList<>();

        visitedNodes.add(targetNodeId);

        Node targetNode = this.graph.nodes.get(targetNodeId);
        for (Edge edge : targetNode.edges.values()) {
            if (edge.isUsed) {
                //edgesToRemove.add(edge);
                idx.add(edge.id);

                // Recursively bomb the neighboring nodes
                int neighborNodeId = edge.getOtherNode(targetNode.id).id;
                idx.addAll(bombNode(neighborNodeId, radius - 1, edgesToRemove, visitedNodes));
            }
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
