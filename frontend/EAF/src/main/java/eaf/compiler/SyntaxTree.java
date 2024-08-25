package eaf.compiler;

import eaf.Main;
import eaf.manager.ColorManager;
import eaf.manager.ExtraRectManager;
import eaf.manager.LogManager;
import eaf.models.Pair;
import eaf.models.*;
import eaf.models.Module;
import eaf.plugin.PluginManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    public static String buildPath = "\\EvoAlBuilds\\" + evoalBuild + "\\evoal\\definitions\\de";

    //=======================================================================
    //                  class name      fieldname     FieldType   Value

    // Next use this to make class trees by when calling extends add a child (but also put new entry that is the same object so same refference in child as in hashmap)
    // When only new type and no extend only put
    //=======================================================================

    private static Deque<File> fileQueue = new ArrayDeque<>();

    public static List<ClassType> getBasedClasses() {
        Collection<ClassType> ar = new ArrayList<>(baseClassRegister.values());
        ar.addAll(ExtraRectManager.baseClassRegister.values());
        return ar.stream().sorted().toList();
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


    static  {
        String currentPath = System.getProperty("user.dir");
        pathToSyntax.add(currentPath + buildPath);
        pathToSyntax.add(currentPath + "\\de");
        for (var plugin : PluginManager.plugins) {
            pathToSyntax.add(PluginManager.getDefinitionsPath(plugin));
        }
    }

    public static void main(String[] args) throws IOException {
        start();

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

        moduleRegister = new HashMap<>();
        classRegister = new HashMap<>();
        baseClassRegister = new HashMap<>();
        constantRegister = new HashMap<>();
        String currentPath = System.getProperty("user.dir");

        for (String path : pathToSyntax) {
            File rootDir = new File(path);
            if (rootDir.exists() && rootDir.isDirectory()) {
                System.out.println(LogManager.syntax() + LogManager.read() + " Building file tree for: " + path);
                buildFileTree(root, rootDir); // Start building the file tree recursively
            } else {
                System.err.println("Directory does not exist or is not a directory: " + path);
            }
        }

        // Example: Printing the tree structure using toString()
        System.out.println(root.toString());

        while (!fileQueue.isEmpty()) {
            File file = fileQueue.poll();
            String relativePath = new File(currentPath).toURI().relativize(file.toURI()).getPath();
            processDlFileForImports(relativePath, makeModuleName(relativePath));
        }


        //processFile("EvoAlScripts\\genetic-programming\\config.ol", "script", false);

        System.out.println("============================");
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



        System.out.println("============================");
        System.out.println("Loaded modules count = " + moduleRegister.size());
        System.out.println("Loaded modules: ");
        System.out.println("============================");
        for (var im : moduleRegister.keySet().stream().sorted().toList()) {
            System.out.println(im);
        }
        System.out.println("============================");
        System.out.println("Adding extra rects ...");
        ExtraRectManager.start();
        System.out.println("============================");
        for (var im :getBasedClasses()) {
            System.out.print(getClassHierarchy(im, "", true, true));
        }
        System.out.println("============================");
        for (var im : constantRegister.values().stream().sorted().toList()) {
            System.out.println(im.toString());
        }
        System.out.println("============================");


    }



    public static String makeModuleName(String s) {
        String definitionName = s.replace(File.separator, ".").replace("/", ".").replace(".dl", "").replace(".ddl", "");
        definitionName =  definitionName.replace("EvoAlBuilds." + evoalBuild + ".evoal.definitions.", "");
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

    public static void processDlFileForImports(String filename, String definitionName) throws IOException {
        if (moduleRegister.get(definitionName) == null) {
            System.out.println(LogManager.imp() + " \"" + definitionName + "\"" + ColorManager.colorText(" is not ", ColorManager.errorColor) + "yet in memory!");
            System.out.println(LogManager.imp() + " Processing definition " + definitionName + " under path " + filename);
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
                        processImport(line, definitions, generator);
                        //System.out.println(line);
                    } else {
                        break;  // Stop processing on the first non-matching line
                    }
                }
                moduleRegister.put(definitionName, new Module(processContentOfModule(new BufferedReader(new FileReader(filename)))));
            }
        }
        else {
            System.out.println(LogManager.imp() + " Skipping \"" + definitionName +"\"" +  ColorManager.colorText(" is ", ColorManager.sucessColor) + "already in memory!");
        }
    }

    public static Pair<String, ArrayList<ClassType>> processContentOfModule(BufferedReader reader) throws IOException {

        ArrayList<ClassType> clazzTypes = new ArrayList<>();
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
        if (moduleName == null) {
            System.out.println(" Module name " + ColorManager.colorText(moduleName, ColorManager.errorColor) + " not found!");
            return new Pair<>(null, clazzTypes);
        }

        // Extract types, extended types, and content within braces
        Pattern typePattern = Pattern.compile("(abstract\\s+)?type\\s+['\"]?([^'\"\\s]+)['\"]?(?:\\s+extends\\s+['\"]?([^'\"\\s]+)['\"]?)?\\s*\\{([^\\{\\}]|\\{[^\\{\\}]*\\})*\\}");
        Matcher typeMatcher = typePattern.matcher(content);
        int i = 0;
        while (typeMatcher.find()) {
            boolean isAbstract = typeMatcher.group(1) != null;
            String typeName = typeMatcher.group(2);


            String extendedType = typeMatcher.group(3);
            typeMatcher.group(0).substring(typeMatcher.group(0).indexOf("{"));
            ClassType clazz = DefineType(typeName, extendedType, moduleName, isAbstract);
            String typeContent = typeMatcher.group(0).substring(typeMatcher.group(0).indexOf("{"));
            //Here needs to be the real version no instance
            processContentOfType(clazz, typeContent);
            clazzTypes.add(clazz);
            i++;
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
            System.out.println(LogManager.type() + " Found constant \"" + name + "\" of type \"" + type + "\" with value \"" + value + "\" in module " + moduleName);
            constantRegister.put(name, new Constant(name, value, type, moduleName));
            constCount++;
        }

        System.out.println(LogManager.type() + " Found " + i + " types and " + constCount + " constants in module " + moduleName);
        return new Pair<>(moduleName, clazzTypes);
    }

    private static String removeComments(String content) {
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

    static void UniversalFieldDefiner(ClassType context, String field, String typename, boolean isInstance, int isArray) {
        System.out.println(LogManager.field() + " DefiningField called with field: " + field + ", typename: " + typename + ", isInstance: " + isInstance+ ", isArray: " + isArray);
        context.addField(field, new FieldType(typename, !isInstance, isArray));
    }

    static void PrimitiveFieldSetter(ClassType context, String field, String typename, String rawValue) {
        typename = getFieldTypeIfNull(context, field, typename);
        System.out.println(LogManager.field() + " FieldSetterPrimitive called with field: " + field + ", typename: " + typename + ", value: " + rawValue);
        context.setField(field, primitiveStringToFieldValue(typename, rawValue), true);
    }

    static void InstanceFieldSetter(ClassType context, String field, String typename, String rawValue) {
        typename = getFieldTypeIfNull(context, field, typename);
        System.out.println(LogManager.field() + " FieldSetterInstance called with field: " + field + ", typename: " + typename + ", value: " + rawValue);
        ClassType instanceContext = getInstanceOfClass(typename);
        FieldValue value = processContentOfType(instanceContext, rawValue);
        context.setField(field, value, true);
    }

    static void ArrayFieldSetter(ClassType context, String field, String typename, String rawValue, boolean defineAndSet) {
        typename = getFieldTypeIfNull(context, field, typename);
        System.out.println(LogManager.field() + " ArraySetter called with field: " + field + " type: " + typename + ", value: " + rawValue);
        if (!rawValue.isEmpty()) {
            int arrayDepth ;
            boolean instance;
            if (defineAndSet) {
                arrayDepth = typename.split("array").length - 1;
                instance = typename.contains("instance");
            }
            else {
                arrayDepth = getArrayDepth(rawValue);
                instance = !context.fields.get(field).getFirst().primitive;
            }
            FieldValue value = processArrayField(new FieldType(typename, !instance, arrayDepth), rawValue);
            context.setField(field, value, true);
        }
        else {
            context.setField(field, null, true);
        }
    }

    public static int getArrayDepth(String input) {
        int maxDepth = 0;
        int currentDepth = 0;

        for (char ch : input.toCharArray()) {
            if (ch == '[') {
                currentDepth++;
                if (currentDepth > maxDepth) {
                    maxDepth = currentDepth;
                }
            } else if (ch == ']') {
                currentDepth--;
            }
        }

        return maxDepth;
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




    public static FieldValue processArrayField(FieldType fieldType, String input) {
        ArrayList<FieldValue> values = new ArrayList<>();
        // Regex patterns
        Pattern structuredPattern = Pattern.compile("(\\w+-\\w+\\s*\\{.*?\\})", Pattern.DOTALL);
        Pattern stringPattern = Pattern.compile("\"([^\"]*)\"");
        Pattern intPattern = Pattern.compile("\\b\\d+\\b");
        Pattern floatPattern = Pattern.compile("\\b\\d+\\.\\d+\\b");

        for (String item : extractArrayElements(input, "[", "]", ",", false)) {
            Matcher instanceMatcher = structuredPattern.matcher(item);
            Matcher stringMatcher = stringPattern.matcher(item);
            Matcher intMatcher = intPattern.matcher(item);
            Matcher floatMatcher = floatPattern.matcher(item);

            if (item.startsWith("[") && item.endsWith("]")) {
                System.out.println(LogManager.parsing() + " Parsing sub array: " + item);
                values.add(processArrayField(fieldType, item));
            } else if (instanceMatcher.find()) {
                String type = item.split("\\s*\\{")[0];
                String value = item.replace(type, "");
                printArrayElement(type, value);
                ClassType instanceContext = getInstanceOfClass(type);
                values.add(processContentOfType(instanceContext, value));
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
            } else {
                throw new UnknownTypeException("Unknown type in array: " + item);
            }
        }
        return new FieldValue(fieldType, values);
    }

    public static FieldValue processContentOfType(ClassType context, String input) {
        input = input.replace("'", "");

        System.out.println(LogManager.parsing() + " Parsing: ");
        System.out.println(input);

        Pattern definingFieldPattern = Pattern.compile("\\b\\b.+:\\b.");
        Pattern fieldSetterPrimitivePattern = Pattern.compile("\\b\\w+:=\\w+(\\.\\w+)?\\b");
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
                ArrayFieldSetter(context, fieldAndType[0], fieldAndType[1], headAndValue[1], true);
            }
            else if (arraySetterPatternPatternMatcher.find() && item.endsWith("]")) {
                String typename = "null";
                String[] fieldAndValue = item.split(":=", 2);
                ArrayFieldSetter(context, fieldAndValue[0], typename, fieldAndValue[1], false);
            }
            else if (fieldSetterInstancePatternMatcher.find() && item.endsWith("}")) {
                var headAndValue = item.split(":=", 2);
                var typeAndValue = headAndValue[1].split("\\{", 2);
                InstanceFieldSetter(context, headAndValue[0], typeAndValue[0].replace("instance", ""), "{" + typeAndValue[1]);
            }
            else if (fieldSetterPrimitivePatternMatcher.find()) {
                var headAndValue = item.split(":=", 2);
                var fieldAndType = headAndValue[0].split(":", 2);
                String type = "null";
                if (fieldAndType.length > 1) {
                    type = fieldAndType[1];
                }
                PrimitiveFieldSetter(context, fieldAndType[0], type, headAndValue[1]);
            }
            else if (definingFieldPatternMatcher.find()) {
                var fieldAndType = item.split(":", 2);
                int arrayCount = item.split("array").length - 1;
                boolean isInstance = item.contains("instance");
                UniversalFieldDefiner(context, fieldAndType[0], fieldAndType[1].replace("array", "array "), isInstance, arrayCount);
            }
            else {
                throw new UnknownTypeException("Unknown type in array: " + item);
            }
        }
        return new FieldValue(context);
    }

    public static void printArrayElement(String typename, String value) {
        System.out.println(LogManager.parsing() + " Array Element: type= " + typename + " value= " + value);
    }





    public static void processImport(String line, String definitions, String generator) throws IOException {
        switch (definitions) {
            case "definitions" :
                TreeNode foundNode = root.findNodeByPath(generator);
                processDlFileForImports(foundNode.fullPath, generator);
                return;
            case "data" :
                return;
            default:
                throw new InvalidImportException("Invalid import statement: " + line);

        }
    }



    public static ClassType DefineType(String typeName, String parent, String module, boolean isAbstract) {
        System.out.println(LogManager.type() + " Registering Type: " + module + ", Type: " + typeName + ", Extending Type: " + parent + ", isAbstract: " + isAbstract);
        var test = classRegister.get(typeName);
        System.out.println(LogManager.pack() + " " + module);
        if (test != null && test.pack.equals(module)) {
            throw new TypeNameAlreadyUsedException("Type \"" + typeName + "\" in module \"" + module + "\" already defined!");
        }
        ClassType parentType = null;

        if (parent != null && !parent.isEmpty()) {
            var res = classRegister.get(parent);
            if (res != null) {
                parentType = res;
            }
            else {
                throw new ParentClassDoesNotExistException("Parent class \"" + parent + "\" for the type \"" + typeName + "\" not found!");
            }
        }
        ClassType c = new ClassType(typeName, parentType, module);
        c.setAbstract(isAbstract);
        classRegister.put(typeName, c);
        if (parentType == null) {
            baseClassRegister.put(typeName, c);
        }
        else {
            parentType.addChild(c);
        }
        return c;
    };


    public static class TypeNameAlreadyUsedException extends RuntimeException {
        public TypeNameAlreadyUsedException(String s) {
            super(s);
        }
    }

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


    public static ClassType getInstanceOfClass(String name) {
        try {
            return classRegister.get(name).instance();
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
            return ExtraRectManager.classRegister.get(s);
        }
        return res1;

    }
}