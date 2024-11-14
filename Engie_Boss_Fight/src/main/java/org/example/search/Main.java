package org.example.search;

import org.example.data.DataReader;

public class Main {
    public static void main(String[] args) {
        DataReader reader = new DataReader("Engie_Boss_Fight/data/bagnolet_353p_3844n_4221e.json");

        // Load our data
        reader.loadData();

        // Perform the transformation
        reader.transform();
    }
}