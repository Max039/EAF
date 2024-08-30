package eaf.models;

import org.json.JSONObject;

import java.util.Objects;

public class Constant implements Comparable {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String name;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String type;
    public String pack;
    public Constant(String name, String value, String type, String pack) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.pack = pack;
    }

    @Override
    public String toString() {
        return "Constant{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", type='" + type + '\'' +
                ", pack='" + pack + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constant constant = (Constant) o;
        return Objects.equals(name, constant.name) && Objects.equals(value, constant.value) && Objects.equals(type, constant.type) && Objects.equals(pack, constant.pack);
    }



    @Override
    public int compareTo(Object o) {
        return name.compareTo(((Constant)o).getName());
    }

    public JSONObject toJson() {
        var o = new JSONObject();
        o.put("name", name);
        o.put("type", type);
        o.put("value", value);
        return o;
    }
}
