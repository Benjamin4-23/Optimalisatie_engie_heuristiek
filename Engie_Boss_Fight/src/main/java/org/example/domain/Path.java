package org.example.domain;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class Path<T> {
    public List<T> edges= new LinkedList<>();
    public int cost = 0;
    public void add(int index, T value){
        edges.add(index, value);
    }
    public static Path empty(){
        return new Path();
    }
}
