package org.example.search.lateacceptance;

public class LAList {
    private int size;
    private double[] list;
    private  int counter = 0;

    public LAList(int size){
        this.size = size;
        this.list = new double[size];
    }

    public void fillList(double startValue){
        for(int i=0;i<size;i++){
            list[i] = startValue;
        }
    }

    public void addToBeginOfList(double newValue){
        counter = counter%size;
        list[counter] = newValue;
        counter++;
    }

    public double getLastValueInTheList(){
        counter = counter%size;
        return list[counter];
    }

    public int getSize() {
        return size;
    }

    public double[] getList(){
        return this.list;
    }
}
