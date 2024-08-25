package eaf.plugin;

import java.io.File;
import java.util.ArrayList;

public class PluginManager {

    public static String definitions = "/src/main/resources/de";
    public static ArrayList<Plugin> plugins = null;
    public static String pathToPlugins = "/plugins";

    public PluginManager() {
        String currentPath = System.getProperty("user.dir");
        discoverPlugins(currentPath + pathToPlugins);
    }

    public void discoverPlugins(String directoryPath) {
        plugins = new ArrayList<>();
        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        plugins.add(new Plugin(file.getPath()));
                    }
                }
            }
        }
    }

    public static String getDefinitionsPath(Plugin p) {
        return p.path + definitions;
    }

}
