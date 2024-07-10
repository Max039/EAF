package compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClazzInstance {

    private boolean isAbstract;
    private HashMap<String, ClazzInstance> children = new HashMap<>();

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    public void addChild(ClazzInstance child) {
        // Implementation for adding a child instance
    }

    public boolean matachesType() {
        //Check if this or a child is of that type and is not abstract
        return true;
    }
}
