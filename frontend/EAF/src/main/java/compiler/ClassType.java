package compiler;

import test.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ClassType implements Comparable {

    public HashMap<String, Pair<FieldType, String>> fields;
    String name;

    String spacing = " ";

    private boolean isAbstract = false;

    private boolean extending = false;

    ClassType parent;

    ArrayList<ClassType> children = new ArrayList<>();

    String pack;

    ClassType(String name, ClassType parent, String pack) {
        this.name = name;
        this.parent = parent;
        this.pack = pack;
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
        children.add(child);
    }

    public boolean matchesType(ClassType c) {
        if (this.equals(c)) {
            return true;
        }
        for (var child : children) {
            if (child.equals(c)) {
                return true;
            }
        }
        return false;
    }

    boolean doesExtend() {
        return parent != null;
    }

    Pair<FieldType, String> getFieldPair(String name) {
        return fields.get(name);
    }

    public String getParent() {
        return parent == null ? "null" : parent.toString();
    }

    public String getParentName() {
        return parent == null ? "null" : parent.name;
    }

    public String getChildrenNames() {
        String s = "";
        for (var child : children) {
            if (s.isEmpty()) {
                s = child.name + " [" + child.getChildrenNames() + "]";
            }
            else {
                s += ", " + child.name + " [" + child.getChildrenNames() + "]";
            }
        }
        return s;
    }

    // Method to return the class hierarchy tree as a string in the specified format
    public static String getClassHierarchy(ClassType root, String indent, boolean last, boolean isRoot) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent);
        if (isRoot) {
            sb.append("─── ");
            indent += "    ";
        } else {
            if (last) {
                sb.append("└── ");
                indent += "    ";
            } else {
                sb.append("├── ");
                indent += "│   ";
            }
        }
        var childrenSorted = root.children.stream().sorted().toList();
        sb.append(root.name + " (" + "\u001B[37m" + root.pack  + "\u001B[0m" + ")").append("\n");
        for (int i = 0; i < childrenSorted.size(); i++) {
            sb.append(getClassHierarchy(childrenSorted.get(i), indent, i == childrenSorted.size() - 1, false));
        }
        return sb.toString();
    }


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(spacing + "Class = " + name + " Parent: " + getParentName() + " Children: [" + getChildrenNames() + "]" +  " {\n" + spacing + " Fields: [\n");
        for (var t : fields.entrySet()) {
            s.append(spacing).append("Field = ").append(t.getKey()).append(" : ").append(t.getValue().toString()).append("\n");
        }
        s.append(spacing).append(" ]\n" + spacing + "}");
        return s.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassType classType = (ClassType) o;
        return Objects.equals(pack, classType.pack) && Objects.equals(name, classType.name) && isAbstract == classType.isAbstract && extending == classType.extending;
    }


    @Override
    public int compareTo(Object o) {
        if (o == null || getClass() != o.getClass())  {
            return -1;
        }
        else {
            ClassType c = (ClassType) o;
            int res =  c.name.compareTo(name);
            if (res > 0) {
                res = -1;
            }
            else if (res < 0) {
                res = 1;
            }
            return res;

        }
    }
}
