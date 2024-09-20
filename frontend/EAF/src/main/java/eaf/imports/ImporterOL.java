package eaf.imports;

import eaf.Main;
import eaf.compiler.SyntaxTree;
import eaf.input.InputHandler;
import eaf.manager.FileManager;
import eaf.manager.LogManager;
import eaf.models.ClassType;
import eaf.models.DataField;
import eaf.models.Module;
import eaf.rects.RectFactory;
import eaf.setup.Preset;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.ErrorManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eaf.Main.dataPanel;
import static eaf.Main.savesPath;

public class ImporterOL extends Importer {

    public void importFile(File file, String path) throws Exception {
        FileManager.emptySave();
        String curr = System.getProperty("user.dir");
        String filename = file.getName().split("\\.")[0];

        loadDDLFiles(file);

        var imps = new ArrayList<Module>();
        SyntaxTree.getImports(file.getAbsolutePath(), imps);
        var tempModule = new eaf.models.Module("temp", imps);
        var content = FileManager.getContentOfFile(file).replace("'", "");
        content = SyntaxTree.removeComments(content);

        var problem = SyntaxTree.extractBlock(content, "specify", '{', '}');
        var ea = SyntaxTree.extractBlock(content, "configure", '{', '}');
        var documenting = "";
        if (ea.contains("documenting")) {
            var parts = ea.split("documenting", 2);
            ea = parts[0] + "}";
            documenting = "{ \n documentors" + parts[1];
        }

        var prob = SyntaxTree.processContentOfType(SyntaxTree.get("de.evoal.optimisation.core.problem").instance(), problem, tempModule);
        var probrec = RectFactory.getRectFromClassType(prob.instance);
        Main.mainPanel.leftPanel.addRect(probrec);

        var al = SyntaxTree.processContentOfType(SyntaxTree.get("de.evoal.optimisation.ea.optimisation.evolutionary-algorithm").instance(), ea, tempModule);
        var rec = RectFactory.getRectFromClassType(al.instance);
        Main.mainPanel.leftPanel.addRect(rec);

        var doc = SyntaxTree.processContentOfType(SyntaxTree.get("de.eaf.base.documentor").instance(), documenting, tempModule);
        var docrec = RectFactory.getRectFromClassType(doc.instance);
        Main.mainPanel.leftPanel.addRect(docrec);



        Main.preset = Preset.getPreset("ea");
        FileManager.writeJSONToFile(FileManager.createSave(), path);
        Main.cacheManager.addToBuffer("filesOpened", path);
        InputHandler.actionHandler.saved();

        if (!Main.nogui) {
            FileManager.loadSave(FileManager.readJSONFileToJSON(path));
        }
    }
}
