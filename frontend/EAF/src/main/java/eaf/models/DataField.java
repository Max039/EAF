package eaf.models;

import eaf.compiler.SyntaxTree;
import org.json.JSONObject;

public class DataField {
    private String name;
    private String type;
    private boolean instance;

    public DataField(String name, String type, boolean instance) {
        this.name = name;
        this.type = type;
        this.instance = instance;
    }

    public DataField(JSONObject o) {
        this.name = (String)o.get("name");
        this.type = (String)o.get("type");
        this.instance = (Boolean) o.get("instance");
    }


    @Override
    public String toString() {
        return name;
    }

    public String toFormat() {
        if (instance) {
            return "data '" + name + "' of instance '" + SyntaxTree.toSimpleName(type) + "';";
        } else {
            return type + " data '" + name + "';";
        }
    }

    // Getters for name and type
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isInstance() {
        return instance;
    }


    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        o.put("name", name);
        o.put("type", type);
        o.put("instance", instance);
        return o;
    }
}