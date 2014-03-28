package parisolve;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import parisolve.backend.algorithms.Solver;

public class CommandLineInterface extends AbstractUI {

    @Override
    public void displayInfo(String message) {
        System.out.println(message);
    }

    @Override
    public void displayError(String message) {
        System.err.println(message);
    }

    @Override
    public void run() {
        displayInfo("To get help for the commands possible, type '?' or 'help'");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        while (!"exit".equals(line)) {
            System.out.print("> ");
            try {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                // TODO perfect place for a string-switch or a map
                if ("help".equals(line) || "?".equals(line)) {
                    displayInfo("You can either open [FILENAME] an arena-file or solve [ALGORITHM] a loaded arena using the given algorithm");
                } else if (line.startsWith("open")) {
                    String[] parts = line.split(" ");
                    String file;
                    if (parts.length == 1) {
                        System.out.print("open> ");
                        file = br.readLine();
                    } else {
                        file = parts[1];
                    }
                    loadArenaFromFile(file);
                } else if (line.startsWith("solve")) {
                    String[] parts = line.split(" ");
                    String algorithm;
                    if (parts.length == 1) {
                        displayInfo("Possible Solvers are: "
                                + KnownArenasTest.getAlgorithms());
                        System.out.print("solve> ");
                        algorithm = br.readLine();
                    } else {
                        algorithm = parts[1];
                    }
                    try {
                        @SuppressWarnings("unchecked")
                        Class<? extends Solver> algorithmClass = (Class<? extends Solver>) Class
                                .forName("parisolve.backend.algorithms."
                                        + algorithm);
                        Constructor<? extends Solver> constructor = algorithmClass
                                .getConstructor();
                        Solver instance = constructor.newInstance();
                        fireSolve(instance);
                    } catch (ClassNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                displayError("Error when reading a line.");
            }
        }
    }
}
