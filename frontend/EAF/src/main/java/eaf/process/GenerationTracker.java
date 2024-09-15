package eaf.process;

import eaf.manager.LogManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;

public class GenerationTracker extends ProcessInfoWorker {

    public static int eafPort = 11113;

    public static BufferedReader in = null;

    public static Socket serverSocket = null;

    public int generation;
    public HashMap<String, Double> optimisationVariables;

    public GenerationTracker() {
        super();
        reset();
    }

    public static void connect() throws IOException {
        serverSocket = new Socket("localhost", eafPort); // Connect to the server
        in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
    }

    public static void disconnect() throws IOException {
        serverSocket.close();
        in = null;
    }

    public void reset() {
        optimisationVariables = new HashMap<>();
        generation = 1;
    }

    public void processInfo(ProcessInfo info) {
        if (info.type.equals("variable-value")) {
            optimisationVariables.put(info.description, Double.parseDouble(info.value));
        }
        else if (info.type.equals("generation-over")) {
            generationOver();
        }
    };

    public void generationOver() {
        generation++;
        LogManager.println("Generation: " + generation);
    }


}
