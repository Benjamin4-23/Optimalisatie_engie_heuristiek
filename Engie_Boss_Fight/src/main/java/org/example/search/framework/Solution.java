package org.example.search.framework;

public interface Solution extends Cloneable {
    public abstract double getObjectiveValue();

    public abstract void setObjectiveValue(double value);

    public abstract Object clone();
}
