package org.example.domain;

import java.util.LinkedList;
import java.util.List;


public class ReconnectPair {
    public Node start;
    public Node end;
    public Integer originalId;
    public ReconnectPair(Integer originalId, Node start, Node end){
        this.start = start;
        this.end = end;
        this.originalId = originalId;
    }
    public ReconnectPair(Edge edge){
        this.start = edge.endNode1;
        this.end = edge.endNode2;
        this.originalId = edge.id;
    }
}
