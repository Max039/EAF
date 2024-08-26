package eaf.plugin;

import eaf.executor.ScriptWriter;
import eaf.manager.FileManager;
import eaf.models.Pair;

import java.util.ArrayList;

public class PluginCreator {



    public static void main(String[] args) {
        createBaseForPlugin("test", "de.test");
    }

    public static void createBaseForPlugin(String pluginName, String moduleName) {
        String currentDirectory = System.getProperty("user.dir");
        try {
            FileManager.copyFolder(currentDirectory + "/plugin_base", currentDirectory + "/plugins/" + pluginName);
            ArrayList<Pair<String, String>> pomReplacements = new ArrayList<>();
            pomReplacements.add(new Pair<>("#name#", pluginName));
            pomReplacements.add(new Pair<>("#module#", moduleName));

            var exportsAndOpens = "";
            for (var i : ScriptWriter.evoAlModules) {
                if (exportsAndOpens.isEmpty()) {
                    exportsAndOpens += "\n";
                }
                exportsAndOpens += "\t\t\t";
                exportsAndOpens += "<arg>--add-exports=" + i + "=" + moduleName + "</arg>";
                exportsAndOpens += "\n";
                exportsAndOpens += "\t\t\t";
                exportsAndOpens += "<arg>--add-opens=" + i + "=" + moduleName + "</arg>";
                exportsAndOpens += "\n";
            }

            pomReplacements.add(new Pair<>("#OpenAndExportmodules#", exportsAndOpens));
            FileManager.replaceContentOfFile(currentDirectory + "/plugins/" + pluginName + "/pom.xml" , pomReplacements);


            ArrayList<Pair<String, String>> moduleReplacements = new ArrayList<>();
            moduleReplacements.add(new Pair<>("#module#", moduleName));
            FileManager.replaceContentOfFile(currentDirectory + "/plugins/" + pluginName + "/src/main/java/module-info.java" , moduleReplacements);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
