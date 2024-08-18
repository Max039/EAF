package test;

import compiler.Constant;
import compiler.SyntaxTree;

import java.util.HashMap;

public class ConstantManager {


    public static HashMap<String, Constant> constants;
    public ConstantManager() {
        refreshConstants();
    }

    public static void refreshConstants() {
        constants = new HashMap<>();
        var reg = (HashMap<String, Constant>)SyntaxTree.constantRegister.clone();
        constants.putAll(reg);
    }

}
