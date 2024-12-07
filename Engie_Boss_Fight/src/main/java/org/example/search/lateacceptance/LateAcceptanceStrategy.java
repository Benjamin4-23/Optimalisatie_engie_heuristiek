package org.example.search.lateacceptance;

import org.example.search.MyObjectiveFunction;
import org.example.search.MySolution;
import org.example.search.SwapTwoPaths;
import org.example.search.framework.Move;
import org.example.search.framework.SearchAlgorithm;
import org.example.search.framework.Solution;

public class LateAcceptanceStrategy extends SearchAlgorithm {

    private MyObjectiveFunction function;
    private MySolution currentSolution;
    private MySolution bestSolution;
    private double currentResult;
    private double bestResult;
    private LAList lateAcceptanceList;
    private final int listLength = 6 ;

    public LateAcceptanceStrategy(String path) {
        this.function = new MyObjectiveFunction();
        currentSolution = new MySolution(path);
        bestSolution = currentSolution;
        bestResult = function.evaluate(bestSolution,null);
        System.out.println("Initial result: " + bestResult);
        this.lateAcceptanceList = new LAList(listLength);
    }

    @Override
    public double execute(int numberOfIterations)  {

        //om te beginnen in totaal evalueren
        currentResult = function.evaluate(currentSolution, null);
        this.lateAcceptanceList.fillList(currentResult);
        int teller = 0;
        for (int i = 0; i < numberOfIterations; i++) {
            Move move = new SwapTwoPaths();
            if (currentResult <= bestResult) {
                bestResult = currentResult;
                System.out.println("New best result: " + bestResult);
                lateAcceptanceList.addToBeginOfList(currentResult);
                bestSolution = new MySolution(currentSolution);
                bestSolution.setObjectiveValue(function.evaluate(bestSolution, null));
            }
            else {
                if (currentResult <= lateAcceptanceList.getLastValueInTheList()) {
                    lateAcceptanceList.addToBeginOfList(currentResult);
                }
                else {
                    lateAcceptanceList.addToBeginOfList((currentResult+bestResult)/2);
                    move.undoMove(currentSolution);
                }
            }
            currentResult =  function.evaluate(currentSolution, move);
            if (i % 100 == 0) {
                System.out.println("Iteration #" + i + " " + bestResult + " " + currentResult);
                for(int j=0;j<this.lateAcceptanceList.getSize();j++){
                    System.out.print(this.lateAcceptanceList.getList()[j] + " ");
                }
                System.out.println();
            }
        }
        System.out.println("bestSolution " + function.evaluate(bestSolution,null) + " " + bestResult);
        return bestResult;
    }


    @Override
    public Solution getBestSolution() {
        return bestSolution;
    }


    @Override
    public Solution getCurrentSolution() {
        return currentSolution;
    }

}
