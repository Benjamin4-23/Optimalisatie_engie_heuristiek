package org.example.validator;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.example.search.Main;

public class Validator {
    public static void main(String args[]) throws Exception {
        for (File file: new File("data").listFiles()) {
            System.out.println("\u001B[34mStart running "+file.getName()+"\u001B[0m");
            String outputfile = "output/output_"+file.getName();

            Main.main(new String[]{file.getPath(), outputfile});

            Process p = Runtime.getRuntime().exec(String.format("py ./visualizer/validator.py %s %s", file.getPath(), outputfile));

            Thread outputThread = new Thread(() -> {
                try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = outputReader.readLine()) != null) {
                        System.out.println("\u001B[33m"+line+"\u001B[0m");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            Thread errorThread = new Thread(() -> {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println("[ERROR]: " + line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            outputThread.start();
            errorThread.start();
            outputThread.join();
            errorThread.join();

            int exitCode = p.waitFor();
            if(exitCode!=0){
                System.err.println("Something went wrong!! (program didnt exit with code 0)");
            }

            System.out.println("Finished running "+file.getName()+"\n");
        }
    }
}
