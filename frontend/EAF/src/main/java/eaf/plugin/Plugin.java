package eaf.plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Plugin {

    public String path;

    public String name;

    public String moduleName;

    public Plugin(String path) {
        this.path = path;
        var split = path.replace("\\", "/").split("/");
        this.name = split[split.length - 1];
        try {
            this.moduleName = getModuleName(this.path + "/src/main/java/module-info.java");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static String getModuleName(String moduleInfoPath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(moduleInfoPath));

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("module ")) {
                // Extract the module name
                String moduleName = line.substring(7, line.indexOf(" {")).trim();
                return moduleName;
            }
        }

        return null; // Return null if no module name is found
    }


}
