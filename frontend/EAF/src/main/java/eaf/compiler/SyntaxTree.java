package eaf.compiler;

import eaf.Main;
import eaf.manager.ColorManager;
import eaf.manager.ExtraRectManager;
import eaf.manager.LogManager;
import eaf.models.Pair;
import eaf.models.*;
import eaf.models.Module;
import eaf.plugin.PluginManager;
import eaf.ui.UiUtil;
import jdk.jshell.execution.Util;

import javax.swing.text.Utilities;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eaf.models.ClassType.getClassHierarchy;


public class SyntaxTree {
    public static ArrayList<String> pathToSyntax = new ArrayList<>();
    public static String evoalBuild = Main.evoalVersion;
    public static TreeNode root = new TreeNode("Root", "");



    public static HashMap<String, eaf.models.Module> moduleRegister;

    private static HashMap<String, ClassType> classRegister;
    private static HashMap<String, ClassType> baseClassRegister;

    public static HashMap<String, Constant> constantRegister;

    public static String buildPath = "/" + Main.evoalBuildFolder + "/" + evoalBuild + "/evoal/definitions/de";

    //=======================================================================
    //                  class name      fieldname     FieldType   Value

    // Next use this to make class trees by when calling extends add a child (but also put new entry that is the same object so same refference in child as in hashmap)
    // When only new type and no extend only put
    //=======================================================================

    private static Deque<File> fileQueue = new ArrayDeque<>();

    public static List<ClassType> getBasedClasses() {
        Collection<ClassType> ar = new ArrayList<>(baseClassRegister.values());
        ar.addAll(ExtraRectManager.baseClassRegister.values());
        return ar.stream().sorted(Comparator.comparing(t -> SyntaxTree.toSimpleName(t.name))).toList();
    }

    public static ArrayList<ClassType> getNonAbstractClasses() {
        var arr = new ArrayList<>(List.of(SyntaxTree.classRegister.values().stream().filter(t -> !t.isAbstract).toArray(ClassType[]::new)));
        arr.addAll(List.of(ExtraRectManager.classRegister.values().stream().filter(t -> !t.isAbstract).toArray(ClassType[]::new)));
        return arr;
    }

    public static ArrayList<ClassType> getClasses() {
        var arr = new ArrayList<>(List.of(SyntaxTree.classRegister.values().stream().toArray(ClassType[]::new)));
        arr.addAll(List.of(ExtraRectManager.classRegister.values().stream().toArray(ClassType[]::new)));
        return arr;
    }

    public static Boolean inModule(String name) {
        return classRegister.get(name) != null;
    }




    public static void reload() {
        try {
            start();
            Main.mainPanel.folderPanel.reload(getNonAbstractClasses());
            Main.constantManager.reload();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void start() throws IOException {

        String currentPath = System.getProperty("user.dir");
        pathToSyntax = new ArrayList<>();
        pathToSyntax.add(currentPath + buildPath);
        //pathToSyntax.add(currentPath + "\\de");
        for (var plugin : PluginManager.plugins.values()) {

            // Create a File object
            File folder = new File(PluginManager.getDefinitionsPath(plugin));

            // Check if the provided path is a directory
            if (folder.isDirectory()) {
                // List all files and directories inside the folder
                File[] files = folder.listFiles();

                // Check if the list is not null and not empty
                if (files != null) {
                    // Iterate over the files
                    for (File file : files) {
                        // Check if it is a directory
                        if (file.isDirectory() && !file.getName().contains("META-INF")) {
                            pathToSyntax.add(file.getPath());
                        }
                    }
                } else {
                    LogManager.println(LogManager.syntax() + LogManager.warning() + " " + plugin.name + " has no definitions!");
                }
            } else {
                LogManager.println(LogManager.syntax() + LogManager.error() + " " + plugin.name + " has no definitions folder!");
            }
        }

        moduleRegister = new HashMap<>();
        classRegister = new HashMap<>();
        baseClassRegister = new HashMap<>();
        constantRegister = new HashMap<>();

        addFuncType();

        for (String path : pathToSyntax) {
            File rootDir = new File(path);
            if (rootDir.exists() && rootDir.isDirectory()) {
                LogManager.println(LogManager.syntax() + LogManager.read() + " Building file tree for: " + path);
                buildFileTree(root, rootDir); // Start building the file tree recursively
            } else {
                System.err.println("Directory does not exist or is not a directory: " + path);
            }
        }

        // Example: Printing the tree structure using toString()
        LogManager.println(root.toString());

        while (!fileQueue.isEmpty()) {
            File file = fileQueue.poll();
            String relativePath = new File(currentPath).toURI().relativize(file.toURI()).getPath();
            if (relativePath.startsWith("plugins")) {
                relativePath = relativePath.split("src.main.resources.", 2)[1];
            }
            processDlFileForImports(file.getAbsolutePath(), makeModuleName(relativePath));
        }


        //processFile("EvoAlScripts\\genetic-programming\\config.ol", "script", false);

        LogManager.println("============================");
        /**
        ClassType type = new ClassType("ZeTestType", null, "TestPackage");
        processContentOfType(type, "{ " +
                "    test : int := 5;\n" +
                "    alterers := alterers {\n" +
                "      crossover := [\n" +
                "        'single-point-crossover' {\n" +
                "          probability := 0.3;\n" +
                "        }\n" +
                "      ];\n" +
                "      mutator := [\n" +
                "        'probability-mutator' {\n" +
                "          probability := 0.2;\n" +
                "        },\n" +
                "        'probability-mutator' {\n" +
                "          probability := 0.4;\n" +
                "        }\n" +
                "      ];      \n" +
                "    }; \n" +
                "    tes2t : int; \n" +
                "    tes2t := 7; \n" +
                //"    arr : array int; \n" +
                //"    arr := [50, 25]; \n" +
                "    arr2 : array string; \n" +
                "    arr2 := [\"tests\", \"tests2\"]; \n" +
                "    test3 : array real := [50.8, 25.9]; \n" +
                //"    lol := [[\"tests\"], \"tests2\"]; \n" +
                //"    lol2 : array int := [[1, [2, 3]], 25.9]; \n" +
                "    zzz : instance 'single-point-crossover'" +
                "    zzz := 'single-point-crossover' {\n" +
                "       probability := 0.4;\n" +
                "     };\n" +
                "     ooo : string;\n" +
                "     ooo := test;\n" +
                "}");
         **/

        createBridges();

        LogManager.println("============================");
        LogManager.println("Loaded modules count = " + moduleRegister.size());
        LogManager.println("Loaded modules: ");
        LogManager.println("============================");
        for (var im : moduleRegister.keySet().stream().sorted().toList()) {
            LogManager.println(im);
        }
        LogManager.println("============================");
        LogManager.println("Adding extra rects ...");
        ExtraRectManager.start();
        LogManager.println("============================");
        for (var im :getBasedClasses()) {
            LogManager.print(getClassHierarchy(im, "", true, true));
        }
        LogManager.println("============================");
        for (var im : constantRegister.values().stream().sorted().toList()) {
            LogManager.println(im.toString());
        }
        LogManager.println("============================");


    }

    private static void createBridges() {
        createAbstractBridgeRect("component", new String[]{"de.evoal.generator.generator.noise-data", "de.evoal.generator.generator.distribution", "de.evoal.generator.generator.benchmark"});
    }

    private static void addFuncType() {
        ClassType func = new ClassType("de.eaf.base.func", null, "de.eaf.base");
        func.setAbstract(true);
        classRegister.put(func.name, func);
        baseClassRegister.put(func.name, func);
    }


    public static String makeModuleName(String s) {
        String definitionName = s.replace(File.separator, ".").replace("/", ".").replace(".dl", "").replace(".ddl", "");
        definitionName =  definitionName.replace(Main.evoalBuildFolder + "." + evoalBuild + ".evoal.definitions.", "");
        return definitionName;
    }

    private static void buildFileTree(TreeNode parentNode, File node) {
        if (node.isDirectory()) {
            TreeNode dirNode = new TreeNode(node.getName(), node.getAbsolutePath());
            if (parentNode != null && !parentNode.children.contains(dirNode)) {
                parentNode.children.add(dirNode);
            }
            else {
                dirNode = parentNode.getChild(node.getName());
            }

            File[] subFiles = node.listFiles();
            if (subFiles != null) {
                for (File file : subFiles) {
                    buildFileTree(dirNode, file); // Recursive call for subdirectories
                }
            }
        } else if (node.isFile()) {
            // Process .dl files
            if (node.getName().endsWith(".dl")) {
                TreeNode fileNode = new TreeNode(node.getName(), node.getAbsolutePath());
                if (!parentNode.children.contains(fileNode)) {
                    parentNode.children.add(fileNode);
                }
            }
            fileQueue.add(node);
        }
    }

    public static Module processDlFileForImports(String filename, String definitionName) throws IOException {
        var res = moduleRegister.get(definitionName);
        if (res == null) {
            ArrayList<Module> modulesImported = new ArrayList<>();
            LogManager.println(LogManager.imp() + " \"" + definitionName + "\"" + ColorManager.colorText(" is not ", ColorManager.errorColor) + "yet in memory!");
            LogManager.println(LogManager.imp() + " Processing definition " + definitionName + " under path " + filename);
            getImports(filename, modulesImported);
            var newModule= processContentOfModule(new BufferedReader(new FileReader(filename)), modulesImported);
            moduleRegister.put(definitionName, newModule);
            return newModule;
        }
        else {
            LogManager.println(LogManager.imp() + " Skipping \"" + definitionName +"\"" +  ColorManager.colorText(" is ", ColorManager.sucessColor) + "already in memory!");
            return res;
        }
    }

    public static void getImports(String filename, ArrayList<Module> modulesImported) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();  // Trim leading and trailing whitespace

                // Check if the line is empty
                if (line.isEmpty()) {
                    continue;  // Skip empty lines
                }

                // Define a regex pattern to match "import ... from ..."
                String regex = "^import \"([^\"]+)\" from ([^;]+);$";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(line);

                // If line matches the pattern, process it
                if (matcher.matches()) {
                    String definitions = matcher.group(1);
                    String generator = matcher.group(2);

                    // Remove single quotes
                    definitions = definitions.replace("'", "");
                    generator = generator.replace("'", "");



                    // Call your function and print the line
                    var res2 = processImport(line, definitions, generator);

                    if (res2 != null) {
                        modulesImported.add(res2);
                    }

                    //LogManager.println(line);
                } else {
                    break;  // Stop processing on the first non-matching line
                }
            }
        }
    }

    public static Module processContentOfModule(BufferedReader reader, ArrayList<Module> imports) throws IOException {

        StringBuilder contentBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            contentBuilder.append(line).append("\n");
        }
        String content = contentBuilder.toString();

        // Remove comments delimited by /** */
        content = removeComments(content);

        // Extract module name
        String moduleName = extractModuleName(content);
        var newModule = new Module(moduleName, imports);
        if (moduleName == null) {
            throw new RuntimeException(" Module name " + ColorManager.colorText(moduleName, ColorManager.errorColor) + " not found!");
        }

        // Extract types, extended types, and content within braces
        Pattern typePattern = Pattern.compile("(abstract\\s+)?type\\s+['\"]?([^'\"\\s]+)['\"]?(?:\\s+extends\\s+['\"]?([^'\"\\s]+)['\"]?)?\\s*\\{([^\\{\\}]|\\{[^\\{\\}]*\\})*\\}");
        Matcher typeMatcher = typePattern.matcher(content);
        int i = 0;

        ArrayList<ClassType> posProcess = new ArrayList<>();

        ArrayList<Pair<ClassType, String>> que = new ArrayList<>();
        while (typeMatcher.find()) {
            boolean isAbstract = typeMatcher.group(1) != null;
            String typeName = typeMatcher.group(2);


            String extendedType = typeMatcher.group(3);
            typeMatcher.group(0).substring(typeMatcher.group(0).indexOf("{"));
            ClassType clazz = DefineType(typeName, extendedType, moduleName, isAbstract, newModule);
            String typeContent = typeMatcher.group(0).substring(typeMatcher.group(0).indexOf("{"));
            //Here needs to be the real version no instance

            que.add(new Pair<>(clazz, typeContent));
            newModule.addType(clazz);
            posProcess.add(clazz);
            i++;
        }
        boolean changes = false;
        boolean changesLastTime = false;
        while (!que.isEmpty()) {
            changes = false;
            ArrayList<Pair<ClassType, String>> newQue = new ArrayList<>();
            for (var c : que) {
                try {
                    processContentOfType(c.getFirst(), c.getSecond(), newModule);
                    changes = true;
                }
                catch (Exception e) {
                    if (changesLastTime) {
                        newQue.add(c);
                    }
                    else {
                        throw new RuntimeException(e);
                    }
                }
            }
            changesLastTime = changes;
            que = newQue;
        }


        while (!posProcess.isEmpty()) {
            ArrayList<ClassType> next = new ArrayList<>();
            for (var c : posProcess) {
                if (c.parent == null || c.parent.parentFieldsSet) {
                    c.setParentFields();
                }
                else {
                    next.add(c);
                }
            }
            posProcess = next;
        }

        int constCount = 0;
        String regex = "const\\s*+([^\\s]+)\\s*+([^\\s]+)\\s*:=\\s*+([^\\s]+);";
        //String regex = "const\\s+([\\w'\\s]+)\\s+([\\w'\\s]+)\\s*:=\\s+([\\w.]+);";


        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String type = matcher.group(1);
            String name = matcher.group(2).replace("'", "");
            String value = matcher.group(3).replace("\"", "");
            LogManager.println(LogManager.type() + " Found constant \"" + name + "\" of type \"" + type + "\" with value \"" + value + "\" in module " + moduleName);
            constantRegister.put(name, new Constant(name, value, type, moduleName));
            constCount++;
        }


        int funcCount = 0;
        // Define the regex pattern
        String patternString = "def\\s+([^\\s]+)\\s+([^\\s]+)\\(([^)]*)\\);";


        // Compile the pattern
        Pattern funcPattern = Pattern.compile(patternString);

        // Match the input string
        Matcher funcMatcher = funcPattern.matcher(content.replace("'", ""));

        // Check if the pattern matches
        while (funcMatcher.find()) {


            // Extract groups
            String group1 = funcMatcher.group(1);
            String group2 = funcMatcher.group(2);
            String group3 = funcMatcher.group(3); // This will match everything inside the parentheses

            LogManager.println(LogManager.type() + " Found func: " + group1 + " " + group2 + "(" + group3 + ")");
            var func  = get("de.eaf.base.func");
            ClassType classType = new ClassType(moduleName + "." + group2, func, moduleName);
            if (!group3.isEmpty()) {
                var parts = group3.split(",");
                for (var part : parts) {
                    var p2 = part.trim().split(" ");
                    FieldType type = new FieldType(p2[0], true, 0);
                    classType.addField(p2[1], type);
                }
            }
            if (classRegister.get(classType.name) == null) {
                classRegister.put(classType.name, classType);
                newModule.addType(classType);
            }
            else {
                throw new ClassDomainAlreadyUsedException("The class domain " + classType.name + " is already used!");
            }


            func.children.add(classType);
            funcCount++;
        }

        LogManager.println(LogManager.type() + " Found " + i + " types, " + constCount + " constants and " + funcCount + " functions in module " + moduleName);
        return newModule;
    }

    public static String removeComments(String content) {
        return content.replaceAll("(?s)/\\*\\*.*?\\*/", "");
    }

    private static String extractModuleName(String content) {
        Pattern modulePattern = Pattern.compile("module\\s+([^\\s\\{]+)\\s*\\{");
        Matcher moduleMatcher = modulePattern.matcher(content);
        if (moduleMatcher.find()) {
            return moduleMatcher.group(1);
        }
        return null;
    }

    public static String getFieldTypeIfNull(ClassType context, String field, String typename) {
        if (typename.equals("null")) {
            var f = context.fields.get(field);
            if (f != null) {
                typename = f.getFirst().typeName;
            }
            else {
                throw new FieldNotFoundException("When trying to set field \"" + field + "\" in class \"" + context.name + "\" in package \"" + context.pack + "\" the field was not found!");
            }
        }
        return typename;
    }

    static void UniversalFieldDefiner(ClassType context, String field, String typename, boolean isInstance, int isArray, Module newModule) {
        if (isInstance) {
            typename = UiUtil.repeatString("array ", isArray) + " instance " + newModule.resolveClass(typename.replace("array", "").replace("instance", "").trim()).name;
        }
        LogManager.println(LogManager.field() + " DefiningField called with field: " + field + ", typename: " + typename + ", isInstance: " + isInstance+ ", isArray: " + isArray);
        context.addField(field, new FieldType(typename, !isInstance, isArray));
    }

    static void PrimitiveFieldSetter(ClassType context, String field, String typename, String rawValue) {
        typename = getFieldTypeIfNull(context, field, typename);
        LogManager.println(LogManager.field() + " FieldSetterPrimitive called with field: " + field + ", typename: " + typename + ", value: " + rawValue);
        if (typename.contains("data")) {
            rawValue = rawValue.split("data", 2)[1];
        }
        context.setField(field, primitiveStringToFieldValue(typename, rawValue), true);
    }

    static void InstanceFieldSetter(ClassType context, String field, String typename, String rawValue, Module newModule) {
        var type = newModule.resolveClass(typename);
        LogManager.println(LogManager.field() + " FieldSetterInstance called with field: " + field + ", typename: " + type.name + ", value: " + rawValue);
        ClassType instanceContext = getInstanceOfClass(typename, newModule);
        FieldValue value = processContentOfType(instanceContext, rawValue, newModule);
        context.setField(field, value, true);
    }

    static void ArrayFieldSetter(ClassType context, String field, String typename, String rawValue, boolean defineAndSet, Module newModule) {
        typename = getFieldTypeIfNull(context, field, typename);
        LogManager.println(LogManager.field() + " ArraySetter called with field: " + field + " type: " + typename + ", value: " + rawValue);
        if (!rawValue.isEmpty()) {
            int arrayDepth ;
            boolean instance;
            if (defineAndSet) {
                arrayDepth = 0;
                if (typename.contains("array")) {
                    arrayDepth = typename.split("array").length - 1;
                }
                instance = typename.contains("instance");
            }
            else {

                arrayDepth = getArrayDepth(rawValue);
                instance = !context.fields.get(field).getFirst().primitive;
            }
            if (instance) {
                typename = UiUtil.repeatString("array ", arrayDepth)  + " instance "  + typename;
            }
            FieldValue value = processArrayField(new FieldType(typename, !instance, arrayDepth), rawValue, newModule);
            context.setField(field, value, true);
        }
        else {
            context.setField(field, null, true);
        }
    }

    public static int getArrayDepth(String input) {
        int count = 0;

        // Iterate through each character in the string
        for (char c : input.toCharArray()) {
            if (c == '[') {
                count++;
            } else if (c != ' ') {
                // Stop counting when a non-space character is found
                break;
            }
        }

        return count;
    }

    public static List<String> extractArrayElements(String input, String prefix, String suffix, String separator, boolean keepSeparator) {
        input = input.replace(" ", "");
        if (input.startsWith(prefix) && (input.endsWith(suffix + separator) ||input.endsWith(suffix))) {
            if ((input.endsWith(suffix + separator))) {
                input = input.substring(1, input.length() - 2);
            }
            else {
                input = input.substring(1, input.length() - 1);
            }
        }
        input +=  separator;

        List<String> substrings = new ArrayList<>();
        int curlyBraceCount = 0;
        int squareBracketCount = 0;
        int lastValidPosition = 0;


        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            if (currentChar == '{') {
                curlyBraceCount++;
            } else if (currentChar == '[') {
                squareBracketCount++;
            } else if (currentChar == '}') {
                curlyBraceCount--;
            } else if (currentChar == ']') {
                squareBracketCount--;
            } else if (currentChar == separator.charAt(0)) {
                if (squareBracketCount == 0 && curlyBraceCount == 0) {
                    String s = input.substring(lastValidPosition, i).trim();

                    // Valid comma found, extract substring
                    if (!s.isEmpty()) {
                        if (keepSeparator) {
                            substrings.add(s + separator);
                        }
                        else {
                            substrings.add(s);
                        }

                    }
                    lastValidPosition = i + 1;

                }
            }

        }
        return substrings;
    }




    public static FieldValue processArrayField(FieldType fieldType, String input, Module newModule) {
        ArrayList<FieldValue> values = new ArrayList<>();
        // Regex patterns
        Pattern structuredPattern = Pattern.compile("(\\w+\\s*\\{.*?\\})", Pattern.DOTALL);
        Pattern stringPattern = Pattern.compile("\"([^\"]*)\"");
        Pattern intPattern = Pattern.compile("\\b\\d+\\b");
        Pattern floatPattern = Pattern.compile("\\b\\d+\\.\\d+\\b");

        for (String item : extractArrayElements(input, "[", "]", ",", false)) {
            Matcher instanceMatcher = structuredPattern.matcher(item);
            Matcher stringMatcher = stringPattern.matcher(item);
            Matcher intMatcher = intPattern.matcher(item);
            Matcher floatMatcher = floatPattern.matcher(item);

            if (item.startsWith("[") && item.endsWith("]")) {
                LogManager.println(LogManager.parsing() + " Parsing sub array: " + item);
                printArrayElement("array", item);
                values.add(processArrayField(fieldType, item, newModule));
            } else if (instanceMatcher.find()) {
                String type = item.split("\\s*\\{")[0];
                String value = item.replace(type, "");
                printArrayElement("instance " + type, value);
                ClassType instanceContext = getInstanceOfClass(type, newModule);
                values.add(processContentOfType(instanceContext, value, newModule));
            }
            else if (stringMatcher.find()) {
                printArrayElement("string", stringMatcher.group(1));
                values.add(primitiveStringToFieldValue("string", stringMatcher.group(1)));
            } else if (floatMatcher.find()) {
                printArrayElement("real", floatMatcher.group(0));
                values.add(primitiveStringToFieldValue("real", floatMatcher.group()));
            } else if (intMatcher.find()) {
                printArrayElement("int", intMatcher.group(0));
                values.add(primitiveStringToFieldValue("int", intMatcher.group()));
            } else if (item.contains("data")) {
                var data = item.replace("data", "");
                printArrayElement("data", data);
                values.add(primitiveStringToFieldValue("data", data));
            }
            else {
                throw new UnknownTypeException("Unknown type in array: " + item);
            }
        }
        return new FieldValue(fieldType, values);
    }

    public static FieldValue processContentOfType(ClassType context, String input, Module newModule) {
        input = input.replace("'", "");

        LogManager.println(LogManager.parsing() + " Parsing: ");
        LogManager.println(input);

        Pattern definingFieldPattern = Pattern.compile("\\b\\b.+:\\b.");
        Pattern fieldSetterPrimitivePattern = Pattern.compile("\\b[^\\s\\+]+:=\"?[\\w\\p{L}-]+(\\.[\\w\\p{L}-]+)*\"?\\b");
        Pattern fieldSetterInstancePattern = Pattern.compile("\\w+(?::=\\w+)?(?:\\{(?:[^{}]*\\{[^{}]*\\}[^{}]*)*\\})?");
        Pattern arrayDefinerPattern = Pattern.compile("(\\b.+):(\\b.+):=\\[(?s)(.*?)\\]");
        Pattern arraySetterPattern = Pattern.compile("(\\b.+):=\\[(?s)(.*?)\\]");

        for (String item : extractArrayElements(input, "{", "}", ";", false)) {
            Matcher definingFieldPatternMatcher = definingFieldPattern.matcher(item);
            Matcher fieldSetterPrimitivePatternMatcher = fieldSetterPrimitivePattern.matcher(item);
            Matcher fieldSetterInstancePatternMatcher = fieldSetterInstancePattern.matcher(item);
            Matcher arrayDefinerPatternMatcher = arrayDefinerPattern.matcher(item);
            Matcher arraySetterPatternPatternMatcher = arraySetterPattern.matcher(item);

            if (arrayDefinerPatternMatcher.find() && item.endsWith("]")) {
                var headAndValue = item.split(":=", 2);
                var fieldAndType = headAndValue[0].split(":", 2);
                ArrayFieldSetter(context, fieldAndType[0], fieldAndType[1], headAndValue[1], true, newModule);
            }
            else if (arraySetterPatternPatternMatcher.find() && item.endsWith("]")) {
                String typename = "null";
                String[] fieldAndValue = item.split(":=", 2);
                ArrayFieldSetter(context, fieldAndValue[0], typename, fieldAndValue[1], false, newModule);
            }
            else if (fieldSetterInstancePatternMatcher.find() && item.endsWith("}")) {
                var headAndValue = item.split(":=", 2);
                var typeAndValue = headAndValue[1].split("\\{", 2);
                InstanceFieldSetter(context, headAndValue[0], typeAndValue[0].replace("instance", ""), "{" + typeAndValue[1], newModule);
            }
            else if (fieldSetterPrimitivePatternMatcher.find()) {
                var headAndValue = item.split(":=", 2);
                var fieldAndType = headAndValue[0].split(":", 2);
                String type = "null";
                if (fieldAndType.length > 1) {
                    type = fieldAndType[1];
                }
                PrimitiveFieldSetter(context, fieldAndType[0], type, headAndValue[1].replace("\"", ""));
            }
            else if (definingFieldPatternMatcher.find()) {
                var fieldAndType = item.split(":", 2);
                int arrayCount = item.split("array").length - 1;
                boolean isInstance = item.contains("instance");
                UniversalFieldDefiner(context, fieldAndType[0], fieldAndType[1].replace("array", "array "), isInstance, arrayCount, newModule);
            }
            else {
                throw new UnknownTypeException("Unknown type in type: " + item + " in context " + context.name);
            }
        }
        return new FieldValue(context);
    }

    public static void printArrayElement(String typename, String value) {
        LogManager.println(LogManager.parsing() + " Array Element: type= " + typename + " value= " + value);
    }





    public static Module processImport(String line, String definitions, String generator) throws IOException {
        switch (definitions) {
            case "definitions" :
                TreeNode foundNode = root.findNodeByPath(generator);
                if (foundNode == null) {
                    throw new RuntimeException("Node not found: " + generator);
                }
                return processDlFileForImports(foundNode.fullPath, generator);
            case "data" :
                return null;
            default:
                throw new InvalidImportException("Invalid import statement: " + line);

        }
    }



    public static ClassType DefineType(String typeName, String parent, String module, boolean isAbstract, Module newModule) {
        LogManager.println(LogManager.type() + " Registering Type: " + module + ", Type: " + typeName + ", Extending Type: " + parent + ", isAbstract: " + isAbstract);
        var test = classRegister.get(module + "." + typeName);
        LogManager.println(LogManager.pack() + " " + module);
        if (test != null) {
            throw new ClassDomainAlreadyUsedException("Type \"" + typeName + "\" in module \"" + module + "\" already defined!");
        }
        ClassType parentType = null;

        if (parent != null && !parent.isEmpty()) {
            var res = newModule.resolveClass(parent);
            if (res != null) {
                parentType = res;
            }
            else {
                throw new ParentClassDoesNotExistException("Parent class \"" + parent + "\" for the type \"" + typeName + "\" not found!");
            }
        }
        var name = module + "." + typeName;
        ClassType c = new ClassType(name, parentType, module);
        c.setAbstract(isAbstract);

        if (classRegister.get(name) == null) {
            classRegister.put(name, c);
        }
        else {
            throw new ClassDomainAlreadyUsedException("The class domain " + name + " is already used!");
        }
        if (parentType == null) {
            baseClassRegister.put(name, c);
        }
        else {
            parentType.addChild(c);
        }
        return c;
    };


    public static class ParentClassDoesNotExistException extends RuntimeException {
        public ParentClassDoesNotExistException(String s) {
            super(s);
        }
    }

    public static class ClassTypeNotFoundException extends RuntimeException {
        public ClassTypeNotFoundException(String s) {
            super(s);
        }
    }

    public static class ClassDomainAlreadyUsedException extends RuntimeException {
        public ClassDomainAlreadyUsedException(String s) {
            super(s);
        }
    }

    public static class FieldAlreadyDefinedException extends RuntimeException {
        public FieldAlreadyDefinedException(String s) {
            super(s);
        }
    }

    public static class FieldNotFoundException extends RuntimeException {
        public FieldNotFoundException(String s) {
            super(s);
        }
    }

    public static class FieldTypeMismatchException extends RuntimeException {
        public FieldTypeMismatchException(String s) {
            super(s);
        }
    }

    public static class InvalidImportException extends RuntimeException {
        public InvalidImportException(String s) {
            super(s);
        }
    }

    public static class UnknownTypeException extends RuntimeException {
        public UnknownTypeException(String s) {
            super(s);
        }
    }

    public static class BridgeException extends RuntimeException {
        public BridgeException(String s) {
            super(s);
        }
    }


    public static ClassType getInstanceOfClass(String name, Module newModule) {
        try {
            return newModule.resolveClass(name).instance();
        }
        catch (Exception e) {
            throw new ClassTypeNotFoundException(name + " was not found!");
        }
    }

    public static FieldValue primitiveStringToFieldValue(String type, String value) {
        return new FieldValue(type, value);
    }


    public static ClassType get(String s) {
        var res1 = SyntaxTree.classRegister.get(s);
        if (res1 == null) {
            res1 = ExtraRectManager.classRegister.get(s);
        }
        if (res1 == null) {
            throw new ClassTypeNotFoundException("No class was found for " + s);
        }
        return res1;

    }

    public static ClassType getByEnd(String s) {
        ClassType res = null;
        for (var e : SyntaxTree.classRegister.values()) {
            if (e.name.endsWith(s)) {
                res = e;
                break;
            }
        }
        if (res == null) {
            for (var e : ExtraRectManager.classRegister.values()) {
                if (e.name.endsWith(s)) {
                    res = e;
                    break;
                }
            }
        }
        if (res == null) {
            throw new ClassTypeNotFoundException("No class was found for " + s);
        }
        return res;
    }


    public static String toSimpleName(String s) {
        var parts = s.split("\\.");
        return parts[parts.length - 1];
    }

    public static void createAbstractBridgeRect(String name, String[] classesToBridge) {
        ClassType bridge = new ClassType("de.eaf.bridge." + name, null, "de.eaf.bridge");
        bridge.setAbstract(true);
        for (int i = 0; i < classesToBridge.length; i++) {
            var c = get(classesToBridge[i]);
            if (c.parent == null) {
                c.parent = bridge;
                bridge.addChild(c);
            }
            else {
                throw new BridgeException("Cannot create bridge " + bridge.name + " " + c.name + " already has a parent!");
            }
        }
        if (classRegister.get(bridge.name) == null) {
            classRegister.put(bridge.name, bridge);
            baseClassRegister.put(bridge.name, bridge);
        }
        else {
            throw new ClassDomainAlreadyUsedException("The class domain " + bridge.name + " is already used!");
        }
    }

    public static String extractBlock(String text, String target) {
        // Find the index of the target string in the input text
        int targetIndex = text.indexOf(target);

        // If the target string is not found, return null
        if (targetIndex == -1) {
            return null;
        }

        // Find the index of the first '{' after the target string
        int startIndex = text.indexOf("{", targetIndex);

        // If there's no '{' after the target string, return null
        if (startIndex == -1) {
            return null;
        }

        // Initialize a StringBuilder to store the block content
        StringBuilder block = new StringBuilder();

        // Initialize a counter for tracking curly braces
        int braceCounter = 0;

        // Iterate over the characters starting from the first '{'
        for (int i = startIndex; i < text.length(); i++) {
            char currentChar = text.charAt(i);
            block.append(currentChar); // Add the character to the block

            // Increment or decrement braceCounter based on the character
            if (currentChar == '{') {
                braceCounter++;
            } else if (currentChar == '}') {
                braceCounter--;
            }

            // If the braceCounter reaches 0, return the block
            if (braceCounter == 0) {
                return block.toString();
            }
        }

        // If the loop ends and the counter never reached 0, return null (invalid block)
        return null;
    }


    public static ArrayList<String> getFormatParts(String input, String startString, String ... endingStrings) {
        ArrayList<String> result = new ArrayList<>();
        int index = 0;

        startString = " " + startString + " ";

        // Loop through the input string
        while (index < input.length()) {
            // Find the next occurrence of the startString
            int startIndex = input.indexOf(startString, index);
            if (startIndex == -1) {
                // No more startString found, break out of the loop
                break;
            }

            // Move the index to the character after the found startString
            startIndex += startString.length();

            // Find the nearest endingString from the current startIndex
            int endIndex = -1;
            for (String end : endingStrings) {
                int tempEndIndex = input.indexOf(end, startIndex);
                if (tempEndIndex != -1 && (endIndex == -1 || tempEndIndex < endIndex)) {
                    endIndex = tempEndIndex;
                }
            }

            // If an endingString is found, extract the part between startString and the endingString
            if (endIndex != -1) {
                result.add(input.substring(startIndex, endIndex));
                // Move the index to the character after the found endingString
                index = endIndex + 1;
            } else {
                // No endingString found, stop processing
                break;
            }
        }

        return result;
    }

}