package compiler;

import test.Pair;

import java.util.HashMap;

public class ClassType {

    public HashMap<String, Pair<FieldType, String>> fields;
    String name;

    String spacing = " ";

    private boolean isAbstract = false;

    private boolean extending = false;

    ClassType parent;
    ClassType(String name, ClassType parent) {
        this.name = name;
        this.parent = parent;
        fields = new HashMap<>();
        if ( parent != null ) {
            fields.putAll(parent.fields);
        }
    }

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    public void setExtending(boolean extending) {
        this.extending = extending;
    }

    public void addChild(ClassType child) {
        // Implementation for adding a child instance
    }

    public boolean matachesType(ClassType c) {
        //Check if this or a child is of that type and is not abstract
        return true;
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
        StringBuilder s = new StringBuilder(spacing + "Class = " + name + " Parent: " + getParent()  + " {\n" + spacing + " Fields: [\n");
        for (var t : fields.entrySet()) {
            s.append(spacing).append("Field = ").append(t.getKey()).append(" : ").append(t.getValue().toString()).append("\n");
        }
        s.append(spacing).append(" ]\n" + spacing + "}");
        return s.toString();
    }
}
