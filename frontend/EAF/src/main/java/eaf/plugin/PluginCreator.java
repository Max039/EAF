package eaf.plugin;

import eaf.Main;
import eaf.compiler.SyntaxTree;
import eaf.executor.ClassLocator;
import eaf.executor.OpenIntelliJProject;
import eaf.manager.FileManager;
import eaf.manager.LogManager;
import eaf.models.ClassType;
import eaf.models.Pair;
import eaf.ui.UiUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

public class PluginCreator {
    public static HashMap<String, Pair<String, ClassType>> examples;

    public static String pathToExamples = "/class_examples";

    public static Plugin currentPlugin = null;


    public PluginCreator() {
        loadExamples();
        //currentPlugin = createBaseForPlugin("test", "de.test");
    }

    public static void createNewFromExample() {
        if (currentPlugin != null) {
            var o = UiUtil.selectClassType(getCreateAbleClasses());
            if (o != null) {
                openClassMenuAndAddToPlugin(currentPlugin.path, "test", (ClassType) o);
            }
        }
       else {
            noPluginSelected();
        }
    }

    public static void noPluginSelected() {
        System.out.println(LogManager.plugin() + LogManager.error() + " No plugin selected!");
        showMessageDialog(Main.mainFrame, "No selected plugin!", "Error", ERROR_MESSAGE);
    }



    public static List<ClassType> getExampleClasses() {
        return examples.values().stream().map(Pair::getSecond).toList();
    }

    public static List<ClassType> getCreateAbleClasses() {
        ArrayList<ClassType> l = new ArrayList<>();
        for (var e : getExampleClasses()) {
            l.add(e);
            l.addAll(e.getAllDescendants());
        }
        return l;
   }


    private static void findJavaFilesRecursively(File folder, HashMap<String, Pair<String, ClassType>> javaFiles) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                findJavaFilesRecursively(file, javaFiles); // Recursively search in subfolders
            } else if (file.isFile() && file.getName().endsWith(".java")) {
                var name = file.getName().replace(".java", "");
                var found =  SyntaxTree.getByEnd(name);
                if (found != null) {
                    javaFiles.put(file.getName(), new Pair<>(file.getAbsolutePath(), found)); // Add the .java file path to the list
                }
                else {
                    System.out.println(LogManager.plugin() + LogManager.error() + " Name of example class " + name + " not found in Class Register!");
                }

            }
        }
    }


    public static void loadExamples() {
        examples = new HashMap<>();
        String currentDirectory = System.getProperty("user.dir");
        File folder = new File(currentDirectory + pathToExamples);

        if (folder.exists() && folder.isDirectory()) {
            findJavaFilesRecursively(folder, examples);
        }
    }

    public static HashMap<String, Pair<String, ClassType>> getExamples() {
        return examples;
    }

    public static String compileArgs(String moduleName) {
        var exportsAndOpens = "";
        for (var i : ClassLocator.evoalModules) {
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
        return exportsAndOpens;
    }

    public static Plugin createBaseForPlugin(String pluginName, String moduleName) {
        ClassLocator.refreshEvoAlModules();
        String currentDirectory = System.getProperty("user.dir");
        try {
            FileManager.copyFolder(currentDirectory + "/plugin_base", currentDirectory + "/plugins/" + pluginName);
            ArrayList<Pair<String, String>> pomReplacements = new ArrayList<>();
            pomReplacements.add(new Pair<>("#name#", pluginName));
            pomReplacements.add(new Pair<>("#module#", moduleName));



            pomReplacements.add(new Pair<>("#OpenAndExportmodules#", compileArgs(moduleName)));
            FileManager.replaceContentOfFile(currentDirectory + "/plugins/" + pluginName + "/pom.xml" , pomReplacements);


            ArrayList<Pair<String, String>> moduleReplacements = new ArrayList<>();
            moduleReplacements.add(new Pair<>("#module#", moduleName));
            FileManager.replaceContentOfFile(currentDirectory + "/plugins/" + pluginName + "/src/main/java/module-info.java" , moduleReplacements);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Main.pluginManager.discoverPlugins();
        return PluginManager.plugins.get(pluginName);
    }

    public static void openClassMenuAndAddToPlugin(String pluginFolder, String name, ClassType parent) {
        if (currentPlugin != null) {
            addClass(pluginFolder, name, UiUtil.openClassEditorAndReturn(parent, true, false));
        }
        else {
            noPluginSelected();
        }

    }


    public static void updateCompilerArgs() throws Exception {
        System.out.println(LogManager.plugin() + " Compiler args in Pom for plugin \"" + currentPlugin.name + "\"");

        // Load the XML file
        File pomFile = new File(currentPlugin.path + "/pom.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(pomFile);

        // Normalize the document
        doc.getDocumentElement().normalize();

        // Find the <compilerArgs> element
        NodeList pluginList = doc.getElementsByTagName("plugin");
        for (int i = 0; i < pluginList.getLength(); i++) {
            Node pluginNode = pluginList.item(i);
            if (pluginNode.getNodeType() == Node.ELEMENT_NODE) {
                Element pluginElement = (Element) pluginNode;
                Node artifactIdNode = pluginElement.getElementsByTagName("artifactId").item(0);
                if (artifactIdNode != null && "maven-compiler-plugin".equals(artifactIdNode.getTextContent())) {
                    Element configurationElement = (Element) pluginElement.getElementsByTagName("configuration").item(0);
                    if (configurationElement != null) {
                        Element compilerArgsElement = (Element) configurationElement.getElementsByTagName("compilerArgs").item(0);

                        // Remove existing <arg> elements
                        if (compilerArgsElement != null) {
                            NodeList argNodes = compilerArgsElement.getElementsByTagName("arg");
                            while (argNodes.getLength() > 0) {
                                compilerArgsElement.removeChild(argNodes.item(0));
                            }
                        } else {
                            // If <compilerArgs> doesn't exist, create it
                            compilerArgsElement = doc.createElement("compilerArgs");
                            configurationElement.appendChild(compilerArgsElement);
                        }


                        for (var m : ClassLocator.evoalModules) {
                            Element open = doc.createElement("arg");
                            open.setTextContent("--add-opens=" + m + "=" + currentPlugin.moduleName);
                            compilerArgsElement.appendChild(open);

                            Element export = doc.createElement("arg");
                            export.setTextContent("--add-exports=" + m + "=" + currentPlugin.moduleName);
                            compilerArgsElement.appendChild(export);
                        }

                    }
                }
            }
        }

        // Write the updated XML back to the file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(pomFile);
        transformer.transform(source, result);
    }



    public static void addClass(String pluginFolder, String name, ClassType type) {
        String pack = type.pack;
        //String module = type.toModule();

        OpenIntelliJProject.openProject(pluginFolder);
    }



}
