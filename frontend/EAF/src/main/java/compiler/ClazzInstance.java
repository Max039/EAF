package compiler;

import java.util.List;

public class ClazzInstance {

    public boolean isAbstract;

    public List<TreeNode> children;

    public boolean matachesType() {
        //Check if this or a child is of that type and is not abstract
        return true;
    }
}
