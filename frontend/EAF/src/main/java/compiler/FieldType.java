package compiler;

public class FieldType {
    boolean primitive;
    String typeName;
    int arrayCount = 0;

    String spacing = " ";

    @Override
    public String toString() {
        return spacing + "FieldType = " + typeName + " primitive: " + primitive + " array count: " +arrayCount;
    }



}
