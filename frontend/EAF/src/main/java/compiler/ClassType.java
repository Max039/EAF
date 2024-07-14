package compiler;

import test.Pair;

import java.util.HashMap;

public class ClassType {

    HashMap<String, Pair<FieldType, String>> fields;
    String name;

    String spacing = " ";

    ClassType parent;
    ClassType(String name, ClassType parent) {
        this.name = name;
        this.parent = parent;
        fields = new HashMap<>();
    }

    boolean doesExtend() {
        return parent == null;
    }

    Pair<FieldType, String> getFieldPair(String name) {
        return fields.get(name);
    }

    public String getParent() {
        return parent == null ? "null" : parent.toString();
    }


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(spacing + "Class = " + name + " Parent: " + getParent()  + " {\n" + spacing + " Value: {\n");
        for (var t : fields.entrySet()) {
            s.append(spacing).append("Field = ").append(t.getKey()).append(" : ").append(t.getValue().toString()).append("\n");
        }
        s.append(spacing).append(" }\n" + spacing + "}");
        return s.toString();
    }
}
