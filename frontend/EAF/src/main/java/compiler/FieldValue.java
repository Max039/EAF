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
    FieldValue(ClassType clazzType, ArrayList<Pair<String, FieldValue>> values) {
        this.instance = clazzType.instance();
        for (var v : values) {
            instance.setField(v.getFirst(), v.getSecond());
        }
        this.type = new FieldType(clazzType.name, false, 0);
    }

    //Array Constructor
    FieldValue(FieldType type, ArrayList<FieldValue> values) {
        this.values = values;
        this.type = type;
        for (var v : values) {
            try {
                addArrayElement(v);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addArrayElement(FieldValue v) throws Exception {
        if (type.typeName.equals(v.type.typeName) && type.primitive == v.type.primitive && type.arrayCount - 1 == v.type.arrayCount) {
            values.add(v);
        }
        else {
            throw new Exception("Tried to add array element to type but types are not compatible " + type.toString() + " " + v.type.toString());
        }

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
