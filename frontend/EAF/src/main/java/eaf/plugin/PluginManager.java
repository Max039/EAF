package eaf.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class PluginManager {

    public static String definitions = "/src/main/resources";
    public static HashMap<String, Plugin> plugins = null;
    public static String pathToPlugins = "/plugins";

    public PluginManager() {
        discoverPlugins();
    }

    public void discoverPlugins() {
        String currentPath = System.getProperty("user.dir");
        plugins = new HashMap<>();
        File directory = new File(currentPath + pathToPlugins);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        var plugin = new Plugin(file.getPath());
                        plugins.put(plugin.name, plugin);
                    }
                }
            }
        }
    }

    public static String getDefinitionsPath(Plugin p) {
        return p.path + definitions;
    }

}
