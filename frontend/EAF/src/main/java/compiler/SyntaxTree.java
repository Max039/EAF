package compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SyntaxTree {
    public static ArrayList<String> pathToSyntax = new ArrayList<>();
    public static String evoalBuild = "20240709-152420";
    public static TreeNode root = new TreeNode("Root", "");

    // ANSI escape codes for colors
    public static final String RESET = "\u001B[0m";

    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String BLUE = "\u001B[34m";

    public static final String PURPLE = "\033[0;35m";
    public static final String ORANGE = "\u001B[38;5;214m"; // Note: Standard ANSI doesn't support orange, so this uses an extended code

    // Colored prefixes
    public static String importPrefix = "[" + RESET + GREEN + "Import" + RESET + "] ";
    public static String typePrefix = "[" + RESET + BLUE + "Type" + RESET + "] ";
    public static String fieldPrefix = "[" + RESET + ORANGE + "Field" + RESET + "] ";

    public static String parsingPrefix = "[" + RESET + PURPLE + "Parse" + RESET + "] ";

    public static HashMap<String, Definition> definitionsInMemory = new HashMap<>();

    public static String buildPath = "\\EvoAlBuilds\\" + evoalBuild + "\\evoal\\definitions\\de";

    //=======================================================================
    public static HashMap<String, HashMap<String, ClazzInstance>> classTree = new HashMap<>();
    // Next use this to make class trees by when calling extends add a child (but also put new entry that is the same object so same refference in child as in hashmap)
    // When only new type and no extend only put
    //=======================================================================

    private static Deque<File> fileQueue = new ArrayDeque<>();

    public static void main(String[] args) throws IOException {

        String currentPath = System.getProperty("user.dir");

        pathToSyntax.add(currentPath + buildPath);
        pathToSyntax.add(currentPath + "\\de");
        for (String path : pathToSyntax) {
            File rootDir = new File(path);
            if (rootDir.exists() && rootDir.isDirectory()) {
                System.out.println("Building file tree for: " + path);
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

            processDlFileForImports(relativePath, makeModuleName(relativePath), true);
        }


        //processFile("EvoAlScripts\\genetic-programming\\config.ol", "script", false);

        System.out.println("============================");
        System.out.println("============================");
        System.out.println("============================");

        processContentOfType("{ " +
                "    test : int := 5;\n" +
                "    alterers := alterers {\n" +
                "      crossover := [\n" +
                "        'single-node-crossover' {\n" +
                "          probability := 0.3;\n" +
                "        }\n" +
                "      ];\n" +
                "      mutator := [\n" +
                "        'probability-mutator' {\n" +
                "          probability := 0.2;\n" +
                "        },\n" +
                "        'mathematical-expression-rewriter' {\n" +
                "          probability := 0.4;\n" +
                "        }\n" +
                "      ];      \n" +
                "    }; \n" +
                "    tes2t := 7; \n" +
                "    arrary := [50, 25]; \n" +
                "    arrary := [\"tests\", \"tests2\"]; \n" +
                "    test : arrary int := [50.8, 25.9]; \n" +
                "}");


        System.out.println("Loaded modules count = " + definitionsInMemory.size());
        System.out.println("Loaded modules: ");
        System.out.println("============================");
        for (var im : definitionsInMemory.keySet().stream().sorted().toList()) {
            System.out.println(im);
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
            if (node.getName().endsWith(".dl") || node.getName().endsWith(".ddl")) {
                TreeNode fileNode = new TreeNode(node.getName(), node.getAbsolutePath());
                if (!parentNode.children.contains(fileNode)) {
                    parentNode.children.add(fileNode);
                }
            }
            fileQueue.add(node);
        }
    }

    public static void processDlFileForImports(String filename, String definitionName, boolean index) throws IOException {
        System.out.println(importPrefix + "Processing definition " + definitionName + " under path " + filename);
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

            if (index) {
                definitionsInMemory.put(definitionName, new Definition());
                processRestOfFile(new BufferedReader(new FileReader(filename)));
            }


        }


    }

    public static void processRestOfFile(BufferedReader r) throws IOException {
        parseFileContentForTypes(r);
    }


    public static void parseFileContentForTypes(BufferedReader reader) throws IOException {
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
            System.out.println("Module name not found!");
            return;
        }

        // Extract types, extended types, and content within braces
        Pattern typePattern = Pattern.compile("(abstract\\s+)?type\\s+['\"]?([^'\"\\s]+)['\"]?(?:\\s+extends\\s+['\"]?([^'\"\\s]+)['\"]?)?\\s*\\{([^\\{\\}]|\\{[^\\{\\}]*\\})*\\}");
        Matcher typeMatcher = typePattern.matcher(content);

        while (typeMatcher.find()) {
            boolean isAbstract = typeMatcher.group(1) != null;
            String typeName = typeMatcher.group(2);
            String extendedType = typeMatcher.group(3);
            String typeContent = typeMatcher.group(0).substring(typeMatcher.group(0).indexOf("{"));

            if (extendedType == null) {
                processType(moduleName, typeName, isAbstract, typeContent);
            } else {
                processExtendedType(moduleName, typeName, extendedType, isAbstract, typeContent);
            }
        }
    }

    private static String removeComments(String content) {
        return content.replaceAll("(?s)/\\*\\*.*?\\*/", "");
    }

    private static String extractModuleName(String content) {
        Pattern modulePattern = Pattern.compile("module\\s+([\\w.]+)\\s*\\{");
        Matcher moduleMatcher = modulePattern.matcher(content);
        if (moduleMatcher.find()) {
            return moduleMatcher.group(1);
        }
        return null;
    }

    private static void processType(String moduleName, String typeName, boolean isAbstract, String typeContent) {
        System.out.println(typePrefix + "Registering Type: " + moduleName + ", Type: " + typeName + ", isAbstract: " + isAbstract);
        ClazzInstance c = new ClazzInstance();
        c.setAbstract(isAbstract);

        if (classTree.get(typeName) == null) {
            HashMap<String, ClazzInstance> h = new HashMap<>();
            h.put(moduleName, c);
            classTree.put(typeName, h);
        } else {
            classTree.get(typeName).put(moduleName, c);
        }
        // Implement the actual function call here
        processContentOfType(typeContent);

    }

    private static void processExtendedType(String moduleName, String typeName, String extendedType, boolean isAbstract, String typeContent) {
        System.out.println(typePrefix + "Registering Type: " + moduleName + ", Type: " + typeName + ", Extended Type: " + extendedType + ", isAbstract: " + isAbstract);
        ClazzInstance c = new ClazzInstance();
        c.setAbstract(isAbstract);

        if (classTree.get(extendedType) == null) {
            throw new RuntimeException("Cannot extend " + extendedType + " class was not found!");
        } else {
            var s = classTree.get(extendedType);
            for (var sub : s.entrySet()) {
                sub.getValue().addChild(c);
            }
        }

        if (classTree.get(typeName) == null) {
            HashMap<String, ClazzInstance> h = new HashMap<>();
            h.put(moduleName, c);
            classTree.put(typeName, h);
        } else {
            classTree.get(typeName).put(moduleName, c);
        }

        processContentOfType(typeContent);
        // Implement the actual function call here
    }

    // Function declarations as per your requirement
    static void UniversalFieldDefiner(String field, String typename, boolean isInstance, int isArray) {
        System.out.println(fieldPrefix + "DefiningField called with field: " + field + ", typename: " + typename + ", isInstance: " + isInstance+ ", isArray: " + isArray);
    }

    static void PrimitiveFieldSetter(String field, String typename, String value) {
        System.out.println(fieldPrefix + "FieldSetterPrimitive called with field: " + field + ", typename: " + typename + ", value: " + value);
    }

    static void InstanceFieldSetter(String field, String typename, String value) {
        System.out.println(fieldPrefix + "FieldSetterInstance called with field: " + field + ", typename: " + typename + ", value: " + value);
    }

    static void ArrayFieldSetter(String field, String typename, String value) {
        System.out.println(fieldPrefix + "ArraySetter called with field: " + field + " type: " + typename + ", value: " + value);
    }

    public static void processArrayField(String input) {
        // Regex patterns
        Pattern structuredPattern = Pattern.compile("(\\w+-\\w+\\s*\\{.*?\\})", Pattern.DOTALL);
        Pattern stringPattern = Pattern.compile("\"([^\"]*)\"");
        Pattern intPattern = Pattern.compile("\\b\\d+\\b");
        Pattern floatPattern = Pattern.compile("\\b\\d+\\.\\d+\\b");

        // Remove array brackets and split by commas not within curly braces
        String[] items = input.substring(1, input.length() - 1).split(",(?=(?:[^\\{]*\\{[^\\}]*\\})*[^\\}]*$)");

        for (String item : items) {
            item = item.trim();
            Matcher structuredMatcher = structuredPattern.matcher(item);
            Matcher stringMatcher = stringPattern.matcher(item);
            Matcher intMatcher = intPattern.matcher(item);
            Matcher floatMatcher = floatPattern.matcher(item);

            if (structuredMatcher.find()) {
                String type = structuredMatcher.group(1).split("\\s*\\{")[0];
                String value = structuredMatcher.group(1).replace(type, "");
                printArrayElement(type, value);
                processContentOfType(value);
            } else if (stringMatcher.find()) {
                printArrayElement("string", stringMatcher.group(1));
            } else if (floatMatcher.find()) {
                printArrayElement("real", floatMatcher.group(0));
            } else if (intMatcher.find()) {
                printArrayElement("int", intMatcher.group(0));
            } else {
                throw new UnknownTypeException("Unknown type in array: " + item);
            }
        }
    }

    private static String processFieldsOfType(String input, String patternString, String functionName) {
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            switch (functionName) {
                case "DefiningField":
                    String field = matcher.group(1);
                    String arrayPart = matcher.group(2);
                    int arrayCount = arrayPart == null ? 0 : arrayPart.split("array").length - 1;
                    String typename = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
                    typename = typename.replace("instance", "");
                    typename = typename.replace(" ", "");
                    boolean isInstance = matcher.group().contains("instance");
                    UniversalFieldDefiner(field, typename, isInstance, arrayCount);
                    break;
                case "FieldSetterPrimitive":
                    field = matcher.group(1);
                    typename = matcher.group(2);
                    String value = matcher.group(3);
                    PrimitiveFieldSetter(field, typename, value);
                    break;
                case "FieldSetterInstance":
                    field = matcher.group(1);
                    typename = matcher.group(2);
                    value = matcher.group(3);
                    InstanceFieldSetter(field, typename, "{" + value + "}");
                    input = input.replace(matcher.group(), "");
                    processContentOfType("{" + value + "}");
                    break;
                case "ArrayDefiner":
                    value = matcher.group().split(":=", 2)[1].trim(); // Full match including []
                    String s1 = matcher.group().split(":=", 2)[0];
                    var parts = s1.split(":");
                    field = parts[0].trim();
                    typename = parts[1].trim();
                    ArrayFieldSetter(field, typename, value);
                    input = input.replace(matcher.group(), "");
                    processArrayField(value);
                    break;
                case "ArraySetter":
                    value = matcher.group().split(":=", 2)[1].trim(); // Full match including []
                    String s2 = matcher.group().split(":=", 2)[0];
                    var parts2 = s2.split(":");
                    field = parts2[0].trim();
                    typename = "null";

                    ArrayFieldSetter(field, typename, value);
                    input = input.replace(matcher.group(), "");
                    processArrayField(value);
                    break;
            }
        }

        return input;
    }



    public static void printArrayElement(String typename, String value) {
        System.out.println(parsingPrefix + "Array Element: type= " + typename + " value= " + value + ");");
    }

    // Method to parse the input string
    static void processContentOfType(String input) {

        input = input.replace("'", "");

        System.out.println(parsingPrefix + "Parsing: ");
        System.out.println(input);

        // Patterns
        String definingFieldPattern = "(?:['\"])?(\\S+)(?:['\"])?\\s*:\\s*((?:array\\s+)*)((instance\\s+)?(?:['\"])?(\\S+)(?:['\"])?);";
        String fieldSetterPrimitivePattern = "(?:')?(\\w+)(?:')?\\s*(?::\\s*(\\w+(?:\\s*[*\\/+-]\\s*\\w+)*))?\\s*:=\\s*(\"(?:[^\"]|\"\")*\"|[-+]?\\d*\\.?\\d+|\\w+(?:\\s*[*\\/+-]\\s*\\w+)*)\\s*;";
        String fieldSetterInstancePattern = "(\\w+)\\s*:=\\s*(\\w+)\\s*\\{((?:[^{}]*|\\{(?:[^{}]*|\\{[^{}]*\\})*\\})*)\\};";
        String arrayDefinerPattern = "((\\w+)\\s*:\\s*)((\\w+)\\s*)*:=\\s*\\[[^\\[\\]]*\\];";
        String arraySetterPattern = "(\\w+)\\s*:=\\s*\\[[^\\[\\]]*\\];";

        // Match and call functions
        input = processFieldsOfType(input, fieldSetterInstancePattern, "FieldSetterInstance");
        input = processFieldsOfType(input, arrayDefinerPattern, "ArrayDefiner");
        input = processFieldsOfType(input, arraySetterPattern, "ArraySetter");
        input = processFieldsOfType(input, definingFieldPattern, "DefiningField");
        processFieldsOfType(input, fieldSetterPrimitivePattern, "FieldSetterPrimitive");
    }


    public static void processImport(String line, String definitions, String generator) throws IOException {


        switch (definitions) {
            case "definitions" :
                if (definitionsInMemory.get(generator) == null) {
                    System.out.println(importPrefix + generator + RED + " is not " + RESET + "yet in memory!");
                    TreeNode foundNode = root.findNodeByPath(generator);
                    processDlFileForImports(foundNode.fullPath, generator, true);
                }
                else {
                    System.out.println(importPrefix + "Skipping \"" + generator +"\"" +  GREEN + " is " + RESET + "already in memory!");
                }
                return;
            case "data" :
                return;
            default:
                throw new InvalidImportException("Invalid import statement: " + line);

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
}