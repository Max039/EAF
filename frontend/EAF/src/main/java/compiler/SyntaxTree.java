package compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class SyntaxTree {
    public static ArrayList<String> pathToSyntax = new ArrayList<>();
    public static String evoalBuild = "20240709-152420";
    public static TreeNode root = new TreeNode("Root", "");

    public static HashMap<String, DataObject> definitionsInMemory = new HashMap<>();

    //=======================================================================
    public static HashMap<String, ClazzInstance> classTree = new HashMap<>();
    // Next use this to make class trees by when calling extends add a child (but also put new entry that is the same object so same refference in child as in hashmap)
    // When only new type and no extend only put
    //=======================================================================

    public static void main(String[] args) throws IOException {
        String currentPath = System.getProperty("user.dir");
        pathToSyntax.add(currentPath + "\\EvoAlBuilds\\" + evoalBuild + "\\evoal\\definitions\\de");

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

        processFile("EvoAlScripts\\genetic-programming\\config.ol", "script", false);

        System.out.println("Hashmap size = " + definitionsInMemory.size());
    }

    private static void buildFileTree(TreeNode parentNode, File node) {
        if (node.isDirectory()) {
            TreeNode dirNode = new TreeNode(node.getName(), node.getAbsolutePath());
            parentNode.children.add(dirNode);
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
                parentNode.children.add(fileNode);
            }
        }
    }

    public static void processFile(String filename, String definitionName, boolean index) throws IOException {
        System.out.println("Processing definition " + definitionName + " under path " + filename);
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
                    processLine(line, definitions, generator);
                    //System.out.println(line);
                } else {
                    break;  // Stop processing on the first non-matching line
                }
            }

            if (index) {
                definitionsInMemory.put(definitionName, new DataObject());
            }

            processRestOfFile(reader);
        }


    }

    public static void processRestOfFile(BufferedReader r) {

    }

    public static void processLine(String line, String definitions, String generator) throws IOException {
        // Replace this with your actual processing logic
        System.out.println("Definitions: " + definitions);
        System.out.println("Generator: " + generator);

        switch (definitions) {
            case "definitions" :
                System.out.println("definitions found");
                if (definitionsInMemory.get(generator) == null) {
                    System.out.println(generator + " is not yet in memory!");
                    TreeNode foundNode = root.findNodeByPath(generator);
                    processFile(foundNode.fullPath, generator, true);
                }
                else {
                    System.out.println(generator + " is already in memory!");
                }

                return;
            case "data" :
                System.out.println("data found");
                return;
            default:
                throw new RuntimeException("Invalid import statement: " + line);

        }

    }
}