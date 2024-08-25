package eaf.process;

import java.util.HashMap;

public class GenerationTracker extends ProcessInfoWorker {
    public int generation;
    public HashMap<String, Double> optimisationVariables;

    public GenerationTracker() {
        super();
        reset();
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
        System.out.println("Generation: " + generation);
    }


}
