package test;

public class DataField {
    private String name;
    private String type;
    private boolean instance;

    public DataField(String name, String type, boolean instance) {
        this.name = name;
        this.type = type;
        this.instance = instance;
    }

    @Override
    public String toString() {
        if (instance) {
            return "data '" + name + "' of instance '" + type + "';";
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
}