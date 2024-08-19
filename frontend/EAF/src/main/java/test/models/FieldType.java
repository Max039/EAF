package test.models;

import java.util.Objects;

public class FieldType {
    public boolean primitive;
    public String typeName;
    public int arrayCount;

    String spacing = " ";


    public FieldType(String typeName, boolean primitive, int arrayCount) {
        this.typeName = typeName.replace("instance", "").replace("array", "").replace(" ", "");
        this.primitive = primitive;
        this.arrayCount = arrayCount;
    }

    @Override
    public String toString() {
        return spacing + "FieldType = " + typeName + " primitive: " + primitive + " array count: " +arrayCount;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldType fieldType = (FieldType) o;
        return Objects.equals(typeName, fieldType.typeName) && primitive == fieldType.primitive && arrayCount == fieldType.arrayCount;
    }

    public FieldType clone() {
        return new FieldType(typeName, primitive, arrayCount);
    }

}
