package compiler;

import java.util.Objects;

public class FieldType {
    boolean primitive;
    String typeName;
    int arrayCount = 0;

    String spacing = " ";

    @Override
    public String toString() {
        return spacing + "FieldType = " + typeName + " primitive: " + primitive + " array count: " +arrayCount;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldType fieldType = (FieldType) o;
        return primitive == fieldType.primitive && arrayCount == fieldType.arrayCount && Objects.equals(typeName, fieldType.typeName);
    }

}
