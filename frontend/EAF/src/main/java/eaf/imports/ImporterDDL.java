package eaf.imports;

import eaf.Main;
import eaf.compiler.SyntaxTree;
import eaf.input.InputHandler;
import eaf.manager.FileManager;
import eaf.manager.LogManager;
import eaf.models.DataField;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eaf.Main.dataPanel;
import static eaf.Main.savesPath;

public class ImporterDDL extends Importer{

    public void importFile(File file) throws Exception {
        FileManager.emptySave();
        String curr = System.getProperty("user.dir");
        String filename = file.getName().split("\\.")[0];
        String path = curr + "/" + savesPath + "/" + filename + "/" + filename + ".eaf";
        LogManager.println(LogManager.importer() + LogManager.file() + " " +path);

        var imps = new ArrayList<eaf.models.Module>();
        SyntaxTree.getImports(file.getAbsolutePath(), imps);
        var tempModule = new eaf.models.Module("temp", imps);
        var content = FileManager.getContentOfFile(file).replace("'", "");



        // Regex to match type, name, and instance flag
        String regex = "data\\s+(?:(?<name>[\\w\\-]+)\\s+of\\s+instance\\s+(?<typeinstance>\\w+))|(?<type>[\\w\\s]+)\\s+data\\s+(?<name2>\\S+);";
        Pattern pattern = Pattern.compile(regex);


        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String name, type;
            boolean isInstance;

            if (matcher.group("name") != null && matcher.group("typeinstance") != null) {
                // Matches the pattern "data <name> of instance <type>"
                name = matcher.group("name");
                type = matcher.group("typeinstance");
                isInstance = true;
            } else if (matcher.group("type") != null && matcher.group("name2") != null) {
                // Matches the pattern "<type> data <name>"
                name = matcher.group("name2");
                type = matcher.group("type");
                isInstance = false;
            } else {
                // If it doesn't match either, just skip (this shouldn't happen with given inputs)
                continue;
            }

            type = type.replace("\n", "");

            if (isInstance) {
                type = tempModule.resolveClass(type).name;
            }
            type = type.trim();

            // Print out the result
            LogManager.println(LogManager.importer() + LogManager.syntax() + LogManager.data() + " name = " + name + ", type = " + type + ", instance = " + isInstance);
            dataPanel.addDataField(new DataField(name, type, isInstance));
        }

        // === Only used for import of ml, ol and generator
        //FileManager.writeJSONToFile(FileManager.createSave(), path);
        //Main.cacheManager.addToBuffer("filesOpened", path);
        //InputHandler.actionHandler.saved();
    }
}
