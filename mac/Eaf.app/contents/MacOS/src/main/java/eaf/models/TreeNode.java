package eaf.models;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    String name;
    public String fullPath;
    public List<TreeNode> children;

    public TreeNode(String name, String fullPath) {
        this.name = name;
        this.fullPath = fullPath;
        this.children = new ArrayList<>();
    }

    @Override
    public String toString() {
        return toStringHelper(this, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TreeNode) {
            return ((TreeNode) o).name.equals(name);
        }
        return false;
    }

    public TreeNode getChild(String s) {
        for (var c : children) {
            if (c.name.equals(s)) {
                return c;
            }
        }
        return null;
    }

    private String toStringHelper(TreeNode node, int level) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(level)).append("- ").append(node.name).append("\n");
        for (TreeNode child : node.children) {
            sb.append(toStringHelper(child, level + 1));
        }
        return sb.toString();
    }

    private String getIndent(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("\t");
        }
        return sb.toString();
    }
    public TreeNode findNodeByPath(String path) {
        String[] parts = path.split("\\.");

        return findNodeByPathHelper(this, parts, 0);
    }

    private TreeNode findNodeByPathHelper(TreeNode currentNode, String[] parts, int index) {
        if (index == parts.length) {
            return currentNode;
        }
        for (TreeNode child : currentNode.children) {
            if (child.name.equals(parts[index]) || child.name.equals(parts[index] + ".dl")) {
                return findNodeByPathHelper(child, parts, index + 1);
            }
        }
        return null; // Node not found
    }
}