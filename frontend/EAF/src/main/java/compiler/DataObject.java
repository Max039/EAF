package compiler;

import test.Pair;

import java.util.HashMap;

public class DataObject {
    public enum Type {
        OBJECT,
        ARRAY,
        PRIMITIVE

    }

    public String clazzName;


    HashMap<String, Pair<Type, DataObject>> content = new HashMap<>();


}
