package eaf.models;

import eaf.compiler.SyntaxTree;
import eaf.manager.LogManager;
import eaf.rects.multi.ClassRect;

import java.util.ArrayList;
import java.util.HashMap;

public class Module {
    public ArrayList<ClassType> types;

    public String name;

    public ArrayList<Module> importedModules;

    public  HashMap<String, ClassType> classesLoaded;

    public ArrayList<ClassType> getLoadedClasses() {
        return new ArrayList<>(classesLoaded.values().stream().toList());
    }

    public Module(String moduleName, ArrayList<Module> importedModules) {
        this.types = new ArrayList<>();
        this.name = moduleName;
        this.importedModules = importedModules;

        classesLoaded = new HashMap<>();

        ArrayList<String> moduleBlackList = new ArrayList<>();


        // Load all classes from imported modules into classesLoaded
        for (var module : importedModules) {
            loadClasses(module, moduleBlackList);
        }
    }

    public void loadClasses(Module module, ArrayList<String> moduleBlackList) {

        if (!moduleBlackList.contains(module.name)) {
            moduleBlackList.add(module.name);
            // Add all classes from the imported module to classesLoaded
            for (var classType : module.types) {
                var split = classType.getName().split("\\.");
                var name = split[split.length - 1];
                // Assuming ClassType has a getName() method for the key
                if (classesLoaded.get(name) == null) {
                    classesLoaded.put(name, classType);
                }
                else {
                    throw new ModuleDomainException("Ambiguous type definition in module " + this.name + " for " + classType.getName() + " !");
                }
            }
            for (var mod : module.importedModules) {
                loadClasses(mod, moduleBlackList);
            }
        }
    }

    public void addType(ClassType type) {
        var split = type.getName().split("\\.");
        classesLoaded.put(split[split.length - 1], type);
        types.add(type);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Module " + name + " = " + "[\n");
        for (var t : types) {
            s.append(t.toString()).append("\n");
        }
        s.append("]");
        return s.toString();
    }

    public ClassType resolveClass(String className) {
        LogManager.println(LogManager.syntax() + " Trying to resolve " + className + " in module " + name);
        return classesLoaded.get(className);
    }

    public static class ModuleDomainException extends RuntimeException {
        public ModuleDomainException(String s) {
            super(s);
        }
    }

}
