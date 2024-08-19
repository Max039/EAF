package eaf.models;

import java.util.ArrayList;

public class Module {
    ArrayList<ClassType> types = new ArrayList<>();

    String name;

    public Module(Pair<String, ArrayList<ClassType>> types) {
        this.types = (ArrayList<ClassType>) types.getSecond().clone();
        this.name = types.getFirst();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Module " + name + " = " + "[\n");
        for (var t : types) {
            s.append(t.toString()).append("\n");
        }
        s.append("]");
        return s.toString();
    }

}
