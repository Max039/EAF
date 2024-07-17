package compiler;

import test.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static compiler.ClassType.getClassHierarchy;
import static compiler.ClassType.getUniqueImports;


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

    public static HashMap<String, Module> moduleRegister = new HashMap<>();

    public static HashMap<String, ClassType> classRegister = new HashMap<>();
    public static HashMap<String, ClassType> baseClassRegister = new HashMap<>();

    public static String buildPath = "\\EvoAlBuilds\\" + evoalBuild + "\\evoal\\definitions\\de";

    //=======================================================================
    //                  class name      fieldname     FieldType   Value

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
            processDlFileForImports(relativePath, makeModuleName(relativePath));
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
                "    arr := [50, 25]; \n" +
                "    arr := [\"tests\", \"tests2\"]; \n" +
                "    test : array int := [50.8, 25.9]; \n" +
                "    lol := [[\"tests\"], \"tests2\"]; \n" +
                "    lol2 : array int := [[1, [2, 3]], 25.9]; \n" +
                "    zzz := 'mathematical-expression-rewriter' {\n" +
                "       probability := 0.4;\n" +
                "     };\n" +
                "    tttt : instance rewriter := 'rewriter' {\n" +
                "       probability := 0.4;\n" +
                "     };\n" +
                "     ooo := test;\n" +
                "}");



        System.out.println("============================");
        for (var im : moduleRegister.values().stream().map(Module::toString).sorted().toList()) {
            System.out.println(im);
        }
        System.out.println("============================");
        System.out.println("Loaded modules count = " + moduleRegister.size());
        System.out.println("Loaded modules: ");
        System.out.println("============================");
        for (var im : moduleRegister.keySet().stream().sorted().toList()) {
            System.out.println(im);
        }
        System.out.println("============================");
        for (var im : baseClassRegister.values().stream().sorted().toList()) {
            System.out.print(getClassHierarchy(im, "", true, true));
        }
        System.out.println("============================");
        System.out.println("Simulating classes needed for imports: ");
        //ArrayList<ClassType> classesNeededForScript = new ArrayList<>(baseClassRegister.values());
        ArrayList<ClassType> classesNeededForScript = new ArrayList<>();
        classesNeededForScript.add(classRegister.get("thin-plate-spline-svr"));
        classesNeededForScript.add(classRegister.get("generation-print-crossover"));
        System.out.println(getUniqueImports(classesNeededForScript));
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

    public static void processDlFileForImports(String filename, String definitionName) throws IOException {
        if (moduleRegister.get(definitionName) == null) {
            System.out.println(importPrefix + "\"" + definitionName + "\"" + RED + " is not " + RESET + "yet in memory!");
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
                moduleRegister.put(definitionName, new Module(processContentOfModule(new BufferedReader(new FileReader(filename)))));
            }
        }
        else {
            System.out.println(importPrefix + "Skipping \"" + definitionName +"\"" +  GREEN + " is " + RESET + "already in memory!");
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
            System.out.println("Module name not found!");
            return new Pair<>(null, clazzTypes);
        }

        // Extract types, extended types, and content within braces
        Pattern typePattern = Pattern.compile("(abstract\\s+)?type\\s+['\"]?([^'\"\\s]+)['\"]?(?:\\s+extends\\s+['\"]?([^'\"\\s]+)['\"]?)?\\s*\\{([^\\{\\}]|\\{[^\\{\\}]*\\})*\\}");
        Matcher typeMatcher = typePattern.matcher(content);

        while (typeMatcher.find()) {
            boolean isAbstract = typeMatcher.group(1) != null;
            String typeName = typeMatcher.group(2);


            String extendedType = typeMatcher.group(3);
            typeMatcher.group(0).substring(typeMatcher.group(0).indexOf("{"));
            ClassType clazz = DefineType(typeName, extendedType, moduleName, isAbstract);
            String typeContent = typeMatcher.group(0).substring(typeMatcher.group(0).indexOf("{"));
            processContentOfType(typeContent);
            clazzTypes.add(clazz);
        }
        return new Pair<>(moduleName, clazzTypes);
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




    public static void processArrayField(String input) {
        // Regex patterns
        Pattern structuredPattern = Pattern.compile("(\\w+-\\w+\\s*\\{.*?\\})", Pattern.DOTALL);
        Pattern stringPattern = Pattern.compile("\"([^\"]*)\"");
        Pattern intPattern = Pattern.compile("\\b\\d+\\b");
        Pattern floatPattern = Pattern.compile("\\b\\d+\\.\\d+\\b");

        for (String item : extractArrayElements(input, "[", "]", ",", false)) {
            Matcher structuredMatcher = structuredPattern.matcher(item);
            Matcher stringMatcher = stringPattern.matcher(item);
            Matcher intMatcher = intPattern.matcher(item);
            Matcher floatMatcher = floatPattern.matcher(item);

            if (item.startsWith("[") && item.endsWith("]")) {
                System.out.println(parsingPrefix + "Parsing sub array: " + item);
                processArrayField(item);
            } else if (structuredMatcher.find()) {
                String type = item.split("\\s*\\{")[0];
                String value = item.replace(type, "");
                printArrayElement(type, value);
                processContentOfType(value);
            }
            else if (stringMatcher.find()) {
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

    public static void processContentOfType(String input) {
        input = input.replace("'", "");

        System.out.println(parsingPrefix + "Parsing: ");
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
                ArrayFieldSetter(fieldAndType[0], fieldAndType[1].replace("instance", "").replace("array", ""), headAndValue[1]);
                processArrayField(headAndValue[1]);
            }
            else if (arraySetterPatternPatternMatcher.find() && item.endsWith("]")) {
                String typename = "null";
                String[] fieldAndValue = item.split(":=", 2);
                ArrayFieldSetter(fieldAndValue[0], typename, fieldAndValue[1]);
                processArrayField(fieldAndValue[1]);
            }
            else if (fieldSetterInstancePatternMatcher.find() && item.endsWith("}")) {
                var headAndValue = item.split(":=", 2);
                var typeAndValue = headAndValue[1].split("\\{", 2);

                InstanceFieldSetter(headAndValue[0], typeAndValue[0].replace("instance", ""), "{" + typeAndValue[1]);
                processContentOfType("{" + typeAndValue[1]);
            }
            else if (fieldSetterPrimitivePatternMatcher.find()) {
                var headAndValue = item.split(":=", 2);
                var fieldAndType = headAndValue[0].split(":", 2);
                String type = "null";
                if (fieldAndType.length > 1) {
                    type = fieldAndType[1];
                }
                PrimitiveFieldSetter(fieldAndType[0], type, headAndValue[1]);
            }
            else if (definingFieldPatternMatcher.find()) {
                var fieldAndType = item.split(":", 2);
                int arrayCount = item.split("array").length - 1;
                boolean isInstance = item.contains("instance");
                UniversalFieldDefiner(fieldAndType[0], fieldAndType[1].replace("array", "array "), isInstance, arrayCount);
            }
            else {
                throw new UnknownTypeException("Unknown type in array: " + item);
            }
        }
    }

    public static void printArrayElement(String typename, String value) {
        System.out.println(parsingPrefix + "Array Element: type= " + typename + " value= " + value);
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
        System.out.println(typePrefix + "Registering Type: " + module + ", Type: " + typeName + ", Extending Type: " + parent + ", isAbstract: " + isAbstract);
        var test = classRegister.get(typeName);
        System.out.println(module);
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

    public static void DefineField(String clazzTypeName, String field, FieldType fieldType) {
        var clazz = classRegister.get(clazzTypeName);
        if (clazz == null) {
            throw new ClassTypeNotFoundException("When trying to define field \"" + field + "\" for type \"" + clazzTypeName + "\" the type was not found!");
        }
        else {
            if (clazz.getFieldPair(field) == null) {
                System.out.println("Defined field \"" + field + "\" for type \"" + clazzTypeName + "\" with type " + fieldType.toString());
                clazz.addField(field, fieldType);
            } else {
                throw new FieldAlreadyDefinedException("When trying to define field \"" + field + "\" for type \"" + clazzTypeName + "\" field name was already defined!");
            }
        }
    }
    public static void SetField(String clazzTypeName, String field, FieldValue fieldValue) {
        var clazz = classRegister.get(clazzTypeName);
        if (clazz == null) {
            throw new ClassTypeNotFoundException("When trying to set field \"" + field + "\" for type \"" + clazzTypeName + "\" the type was not found!");
        }
        else {
            var res = clazz.getFieldPair(field);
            if (res != null) {
                if (res.getFirst().equals(fieldValue.type)) {
                    if (res.getSecond() == null) {
                        clazz.setField(field, fieldValue);
                    }
                    else {
                        throw new FieldValueAlreadyDefined("When trying to set field \"" + field + "\" for type \"" + clazzTypeName + "\" the value was already defined!");
                    }
                } else {
                    throw new FieldTypeMismatchException("When trying to set field \"" + field + "\" for type \"" + clazzTypeName + "\" there was a type mismatch \"" + fieldValue.type.toString() + "\" != \"" + res.getFirst().toString() + "\"");
                }
            } else {
                throw new FieldNotFoundException("When trying to set field \"" + field + "\" for type \"" + clazzTypeName + "\" the field was not found!");
            }
        }
    }


    public static class FieldValueAlreadyDefined extends RuntimeException {
        public FieldValueAlreadyDefined(String s) {
            super(s);
        }
    }

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
}