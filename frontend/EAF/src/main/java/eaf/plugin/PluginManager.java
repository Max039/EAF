package eaf.plugin;

import java.io.File;
import java.util.ArrayList;

public class PluginManager {

    public static String definitions = "/src/main/resources";
    public static ArrayList<Plugin> plugins = null;
    public static String pathToPlugins = "/plugins";

    public PluginManager() {
        discoverPlugins();
    }

    public void discoverPlugins() {
        String currentPath = System.getProperty("user.dir");
        plugins = new ArrayList<>();
        File directory = new File(currentPath + pathToPlugins);

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
