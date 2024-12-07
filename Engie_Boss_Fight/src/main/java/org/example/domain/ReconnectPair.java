package org.example.domain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class ReconnectPair {
    public Node start;
    public Node end;
    public ArrayList<Integer> ids;
    public ReconnectPair(Node start, Node end){
        this.start = start;
        this.end = end;
        this.ids = new ArrayList<>();
    }
    public ReconnectPair(Edge edge){
        this.start = edge.endNode1;
        this.end = edge.endNode2;
        this.ids = new ArrayList<>() {{add(edge.id);}};
    }
}
