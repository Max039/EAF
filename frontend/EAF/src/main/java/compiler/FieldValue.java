package compiler;

import test.Pair;

import java.util.ArrayList;

public class FieldValue {


    FieldType type;


    String value;
    ArrayList<FieldValue> values;
    ClassType instance;


    FieldValue() {
    }

    //Primitve Constructor
    FieldValue(String typeName, String value) {
        this.value = value;
        type = new FieldType(typeName, true, 0);
    }

    //Instance Constructor
    FieldValue(ClassType clazzType) {
        this.instance = clazzType.instance();
        this.type = new FieldType(clazzType.name, false, 0);
    }

    //Array Constructor
    FieldValue(FieldType type, ArrayList<FieldValue> values) {
        this.values = new ArrayList<>(values); // Create a copy of the values list
        this.type = type;
        for (var v : new ArrayList<>(values)) { // Iterate over the copy of the values list
            try {
                addArrayElement(v);
            } catch (SyntaxTree.FieldTypeMismatchException e) {
                throw new SyntaxTree.FieldTypeMismatchException(e.getMessage());
            }
        }
    }

    private void addArrayElement(FieldValue v) throws SyntaxTree.FieldTypeMismatchException {
        boolean typeCheck;
        if (type.primitive) {
            typeCheck = type.typeName.equals(v.type.typeName);
        } else {
            typeCheck = doesTypesMatch(type, v.type);
        }

        if (typeCheck && type.primitive == v.type.primitive && type.arrayCount - 1 == v.type.arrayCount) {
            values.add(v);
        } else {
            throw new SyntaxTree.FieldTypeMismatchException("Tried to add array element to type but types are not compatible (also consider arraycount - 1 = array count) \n\"" + type.toString() + "\" != \n\"" + v.type.toString() + "\"");
        }
    }

    public static boolean doesTypesMatch(FieldType t1, FieldType t2) {
        var c1 = SyntaxTree.classRegister.get(t1.typeName);
        var c2 = SyntaxTree.classRegister.get(t2.typeName);
        if (c1 == null) {
            throw new SyntaxTree.UnknownTypeException("Could not find type \"" + t1.typeName + "\"");
        }
        if (c2 == null) {
            throw new SyntaxTree.UnknownTypeException("Could not find type \"" + t2.typeName + "\"");
        }

        return c1.matchesType(c2);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(type.toString() + " ");
        if (type.arrayCount > 0) {
            s.append("[");
            for (var v : values) {
                s.append(v.toString());
            }
            s.append("]");
        }
        else if (!type.primitive) {
            s.append("instance: ").append(instance.toString());
        }
        else {
            s.append(" value: ").append(value);
        }
        return s.toString();
    }

    public FieldValue clone() {
        var c = new FieldValue();
        c.instance = instance;
        c.type = type;
        c.value = value;
        c.values = values;
        return c;
    }

}
