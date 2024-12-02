import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.example.domain.Edge;
import org.example.domain.Graph;
import org.example.domain.Node;

public class GraphUtils {

    public static void markReplaceableEdges(Graph graph) {
        for (Edge edge : graph.edges.values()) {
            // Reset the replaceable flag and clear previous replacements
            edge.isReplaceable = false;
            edge.replaceableCost = 0;
            edge.setReplacingEdges(new ArrayList<>());

            // Perform BFS to check for an alternative path
            if (hasAlternativePath(graph, edge.endNode1, edge.endNode2, edge)) {
                edge.isReplaceable = true;
            }
        }
    }

    private static boolean hasAlternativePath(Graph graph, Node start, Node end, Edge ignoredEdge) {
        Queue<Node> queue = new LinkedList<>();
        Map<Node, Integer> distances = new HashMap<>(); // To track the shortest distance to each node
        Map<Node, Edge> pathEdges = new HashMap<>(); // To track the edges used in the path

        queue.add(start);
        distances.put(start, 0); // Distance to start node is 0

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current == end) {
                // Found a path, now backtrack to get the edges
                return backtrackPath(pathEdges, start, end);
            }

            for (Edge edge : current.outgoingEdges.values()) {
                // Ignore the edge we want to check
                if (edge == ignoredEdge) {
                    continue;
                }

                int newDistance = distances.get(current) + edge.cost;

                // Check if we have found a shorter path to edge.endNode2
                if (!distances.containsKey(edge.endNode2) || newDistance < distances.get(edge.endNode2)) {
                    distances.put(edge.endNode2, newDistance);
                    queue.add(edge.endNode2);
                    pathEdges.put(edge.endNode2, edge); // Track the edge used to reach this node
                }
            }
        }

        return false; // No path found
    }

    private static boolean backtrackPath(Map<Node, Edge> pathEdges, Node start, Node end) {
        List<Edge> replacementPath = new ArrayList<>();
        Node current = end;

        while (current != start) {
            Edge edge = pathEdges.get(current);
            if (edge == null) {
                return false; // No valid path found
            }
            replacementPath.add(edge);
            current = edge.endNode1; // Move to the previous node in the path
        }

        // Update the edge with the replacement path details
        Edge lastEdge = replacementPath.get(replacementPath.size() - 1);
        lastEdge.replaceableCost = replacementPath.stream().mapToInt(e -> e.cost).sum();
        lastEdge.setReplacingEdges(replacementPath);

        return true; // Replacement path found
    }
}