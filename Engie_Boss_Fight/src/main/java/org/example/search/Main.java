package org.example.search;

import org.example.data.DataReader;
import org.example.data.OutputWriter;
import org.example.search.framework.SearchAlgorithm;
import org.example.search.hillclimbing.SteepestDescent;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        if (args.length == 2) {
            // via validator
            File inputFile = new File(args[0]);
            SearchAlgorithm steepestDescent = new SteepestDescent(inputFile);
            //steepestDescent.execute(150);
            MySolution bestSolution = (MySolution) steepestDescent.getBestSolution();
            OutputWriter writer = new OutputWriter(bestSolution.getGraph());
            writer.write(args[1], bestSolution.getObjectiveValue());
            return;
        }

        //normal run
        SearchAlgorithm steepestDescent = new SteepestDescent(new File("./data/bagnolet_353p_3844n_4221e.json"));
        //steepestDescent.execute(150);
        MySolution bestSolution = (MySolution) steepestDescent.getBestSolution();
        OutputWriter writer = new OutputWriter(bestSolution.getGraph());
        writer.write("output/output_bagnolet_353p_3844n_4221e.json", bestSolution.getObjectiveValue());
    }
}