package eaf.models;

import eaf.compiler.SyntaxTree;
import eaf.manager.LogManager;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ClassType implements Comparable {

    public HashMap<String, Pair<FieldType, FieldValue>> fields;

    public String getName() {
        return name;
    }

    public String name;

    String spacing = " ";

    public boolean isAbstract = false;

    private boolean extending;

    public ClassType parent;

    public ArrayList<ClassType> children = new ArrayList<>();

    public String pack;

    public boolean parentFieldsSet = false;

    public ClassType(String name, ClassType parent, String pack) {
        this.name = name;
        this.parent = parent;
        this.pack = pack;
        fields = new HashMap<>();
        if ( parent != null ) {
            extending = true;
        }
        else {
            extending = false;
        }
    }

    public void setParentFields() {
        if ( parent != null ) {
            fields.putAll(parent.fields);
        }
        parentFieldsSet = true;
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
            if (child.matchesType(c)) {
                return true;
            }
        }
        return false;
    }

    Pair<FieldType, FieldValue> getFieldPair(String name) {
        return fields.get(name);
    }

    public void addField(String name, FieldType type) {
        var currVal = fields.get(name);
        if (currVal != null && currVal.getSecond() != null) {
            throw new SyntaxTree.FieldAlreadyDefinedException("When trying to define field \"" + name + "\" for type \"" + name + "\" field name was already defined!");
        }
        fields.put(name, new Pair<>(type, null));
    }

    public void setField(String name, FieldValue v, boolean printMsg) {
        var currVal = fields.get(name);
        if (currVal == null) {
            fields.put(name, new Pair<>(v.type, v));
        }
        else {
            if (printMsg) {
                System.out.println(LogManager.field() + "Trying to set field \"" + name + "\" of type \"" + currVal.getFirst() + "\" with current value \"" + currVal.getSecond() + "\" to \"" + v + "\"" );
            }
            boolean typesMatch;
            if (currVal.getFirst().primitive) {
                typesMatch = currVal.getFirst().equals(v.type);
            }
            else {
                typesMatch = FieldValue.doesTypesMatch(currVal.getFirst(), v.type);
            }
            if (typesMatch && currVal.getFirst().primitive == v.type.primitive && currVal.getFirst().arrayCount == v.type.arrayCount) {
                fields.put(name, new Pair<>(v.type, v));
            }
            else {
                throw new SyntaxTree.FieldTypeMismatchException("Tried to add array element to type but types are not compatible \n\"" + currVal.getFirst().toString() + "\" != \n\"" + v.type.toString() + "\"");
            }
        }

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
        sb.append(SyntaxTree.toSimpleName(root.name) + " (" + "\u001B[37m" + root.pack  + "\u001B[0m" + ")").append("\n");
        for (int i = 0; i < childrenSorted.size(); i++) {
            sb.append(getClassHierarchy(childrenSorted.get(i), indent, i == childrenSorted.size() - 1, false));
        }
        return sb.toString();
    }


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(spacing + "Class = " + name + " Parent: " + getParentName() + " Children: [" + getChildrenNames() + "]" +  " {\n" + spacing + " Fields: [\n");
        for (var t : fields.entrySet()) {
            String v = "";
            if (t.getValue().getSecond() != null) {
                v = t.getValue().getSecond().toString();
            }

            s.append(spacing).append("Field = ").append(t.getKey()).append(" : ").append(v).append("\n");
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

    public static String getUniqueImports(ArrayList<ClassType> classTypes) {
        Set<String> packages = new HashSet<>();
        for (ClassType classType : classTypes) {
            collectPackages(classType, packages);
        }

        List<String> sortedPackages = packages.stream().sorted().collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        for (String pack : sortedPackages) {
            sb.append("import \"definitions\" from ").append(pack).append(";\n");
        }
        return sb.toString();
    }

    // Helper method to collect packages from the class and its parents
    private static void collectPackages(ClassType classType, Set<String> packages) {
        if (classType != null) {
            packages.add(classType.pack);
            collectPackages(classType.parent, packages);
        }
    }

    public ClassType instance() {
        var c = new ClassType(name, parent, pack);
        c.setAbstract(isAbstract);
        c.children = children;
        for (var v : fields.entrySet()) {
            var pair = v.getValue();
            FieldValue val = null;
            if (pair.getSecond() != null) {
                val = pair.getSecond().clone();
            }
            c.fields.put(v.getKey(), new Pair<>(pair.getFirst(), val));
        }

        return c;
    }

    public List<ClassType> getAllClassTypes() {
        List<ClassType> result = new ArrayList<>();
        result.add(this);
        for (ClassType child : children) {
            result.addAll(child.getAllClassTypes());
        }
        return result;
    }


    public ClassType findSingleNonAbstractClass() {
        ClassType result = isAbstract ? null : this; // Set result to this class if it's non-abstract, else null
        for (ClassType child : children) {
            ClassType childResult = child.findSingleNonAbstractClass(); // Recursively check each child
            if (childResult != null) {
                if (result != null) {
                    return null; // If more than one non-abstract class is found, return null
                }
                result = childResult; // Otherwise, set result to the non-abstract child
            }
        }
        return result; // Return the single non-abstract class, or null if there's more than one
    }

    public ClassType getRoot() {
        if (parent == null) {
            return this;
        }
        else {
            return parent.getRoot();
        }
    }

    public List<ClassType> getAllDescendants() {
        List<ClassType> descendants = new ArrayList<>();
        collectDescendants(this, descendants);
        return descendants;
    }

    private void collectDescendants(ClassType node, List<ClassType> descendants) {
        for (ClassType child : node.children) {
            descendants.add(child);
            collectDescendants(child, descendants); // Recursively collect the descendants of the child
        }
    }

}
