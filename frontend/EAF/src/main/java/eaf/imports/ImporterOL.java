package eaf.imports;

import eaf.Main;
import eaf.compiler.SyntaxTree;
import eaf.input.InputHandler;
import eaf.manager.FileManager;
import eaf.manager.LogManager;
import eaf.models.DataField;
import eaf.models.Module;
import eaf.rects.RectFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eaf.Main.dataPanel;
import static eaf.Main.savesPath;

public class ImporterOL extends Importer {

    public void importFile(File file) throws Exception {
        FileManager.emptySave();
        String curr = System.getProperty("user.dir");
        String filename = file.getName().split("\\.")[0];
        String path = curr + "/" + savesPath + "/" + filename + "/" + filename + ".eaf";
        LogManager.println(LogManager.importer() + LogManager.file() + " " +path);

        var imps = new ArrayList<Module>();
        SyntaxTree.getImports(file.getAbsolutePath(), imps);
        var tempModule = new eaf.models.Module("temp", imps);
        var content = FileManager.getContentOfFile(file).replace("'", "");
        var problem = SyntaxTree.extractBlock(content, "specify");
        var ea = SyntaxTree.extractBlock(content, "configure");
        var documenting = "";
        if (ea.contains("documenting")) {
            var parts = ea.split("documenting", 2);
            ea = parts[0] + "}";
            documenting = parts[1].substring(0, parts[1].length()-2);
        }
        LogManager.println(problem);
        LogManager.println(ea);
        LogManager.println(documenting);


        var al = SyntaxTree.processContentOfType(SyntaxTree.get("de.evoal.optimisation.ea.optimisation.evolutionary-algorithm").instance(), ea, tempModule);
        var rec = RectFactory.getRectFromClassType(al.instance);
        Main.mainPanel.leftPanel.addRect(rec);

        // Regex to match type, name, and instance flag
        String regex = "data\\s+(?:(?<name>[\\w\\-]+)\\s+of\\s+instance\\s+(?<typeinstance>\\w+))|(?<type>[\\w\\s]+)\\s+data\\s+(?<name2>\\S+);";
        Pattern pattern = Pattern.compile(regex);


        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {

        }

        FileManager.writeJSONToFile(FileManager.createSave(), path);
        Main.cacheManager.addToBuffer("filesOpened", path);
        InputHandler.actionHandler.saved();
    }
}
