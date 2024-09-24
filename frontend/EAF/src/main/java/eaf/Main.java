package eaf;

import eaf.compiler.SyntaxTree;
import eaf.download.Downloader;
import eaf.executor.Executor;
import eaf.executor.OpenIntelliJProject;
import eaf.imports.Importer;
import eaf.input.InputHandler;
import eaf.intro.DoubleHelixAnimation;
import eaf.intro.Intro;
import eaf.intro.SimpleIntro;
import eaf.manager.CacheManager;
import eaf.manager.LogManager;
import eaf.models.Pair;
import eaf.plugin.PluginCreator;
import eaf.plugin.PluginManager;
import eaf.setup.EA;
import eaf.setup.Preset;
import eaf.ui.*;
import eaf.manager.FileManager;
import eaf.ui.panels.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import static eaf.ui.UiUtil.createMenuBar;

public class Main extends JPanel {

    public enum OS {
        MAC,
        WINDOWS
    }

    public static OS os = null;

    public static String version = "1.0.0";

    public static Color bgColor = new Color(49, 51, 53);

    public static Color dividerColor = new Color(35, 35, 35);

    public static Color scrollBarBg = new Color(43, 43, 43);

    public static Color searchBarError = new Color(200, 100, 100);

    public static Color scrollBarButton = new Color(150, 150, 150);

    public static Color scrollBarTopAndBottom = new Color(60, 60, 60);

    public static Color searchBarText = new Color(255, 255, 255);

    public static Color searchBar = new Color(100, 100, 100);
    public static Color searchBarBorder = new Color(85, 85, 85);

    public static Preset preset = null;

    public static ConstantPane constantManager;

    public static ErrorPane errorManager;

    private static final int RECT_SPACING = 5;
    //public static String evoalVersion = "20240708-152016";
    public static String evoalVersion = null;

    public final RectPanel leftPanel = new RectPanel(true);
    public final RectPanel rightPanel = new RectPanel(false);

    public static boolean processRunning = false;

    public static int arrayDefaultCount = 1;

    public JSplitPane mainSplitPane = null;

    public static JPanel borderPanel = null;

    public static JPanel tabsPanel = null;

    public static ConsolePane console = null;

    public static JComponent tab = null;

    public static JPanel tabAndTabs = null;

    public static JSplitPane consoleSplitpane = null;


    public static Main mainPanel = null;

    public static DataFieldListPane dataPanel = null;

    public static JFrame mainFrame = null;

    // Declare the text field and new panel
    public JTextField rightPanelTextField;
    public JTextField leftPanelTextField;
    public JPanel rightContainerPanel;
    public FolderPanel folderPanel;

    public static String saveFormat = "eaf";

    public static String savesPath = "/projects";

    public static String programName = "EvoAl Frontend";

    public static String evoalBuildFolder = "builds";

    public static CacheManager cacheManager;

    public static OpenIntelliJProject openIntelliJProject = new OpenIntelliJProject();

    public static PluginCreator pluginCreator = null;

    public static Intro intro = null;

    public static ArrayList<Pair<String, JComponent>> tabsList;

    public static ArrayList<JButton> tabButtons;

    public static PluginManager pluginManager;

    public synchronized static boolean isIndex() {
        return index;
    }

    public synchronized static void setIndex(boolean index) {
        Main.index = index;
    }

    private static boolean index = false;

    public synchronized static boolean updateChecked() {
        return updateCheck;
    }

    public synchronized static void setUpdateChecked(boolean updateCheck) {
        Main.updateCheck = updateCheck;
    }

    private static boolean updateCheck = false;

    public static boolean ansi = true;

    public static boolean nogui = false;

    public static boolean fulllog = false;

    public static boolean pathArgument = false;

    public static boolean convert = false;

    public static boolean imp = false;

    public static boolean debug = false;

    static {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            os = OS.WINDOWS;
        }
        if (osName.contains("mac")) {
            os = OS.MAC;
        }
    }

    public static void main(String[] args) throws Exception {
        setFont();

        Preset.prepareSetups();
        Importer.prepareImporter();

        if (os == OS.MAC) {
            FileManager.copyToDocuments();
        }

        cacheManager = new CacheManager();
        LogManager.println(LogManager.main() + LogManager.args() + " Args:");
        boolean set = false;
        for (var arg : args) {
            if (arg.startsWith("-")) {
                switch (arg) {
                    case "-noansi" -> ansi = false;
                    case "-nogui" -> nogui = true;
                    case "-import" -> imp = true;
                    case "-convert" -> {
                        convert = true;
                        nogui = true;
                    }
                    case "-fulllog" -> fulllog = true;
                    case "-debug" -> debug = true;
                    default -> {
                        if (arg.contains("-sudopwd")) {
                            var parts = arg.split("=", 2);
                            Executor.ramVersionOfSudo = parts[1];
                        }
                        else {
                            LogManager.println(LogManager.main() + LogManager.args() + " unknown arg: " + arg);
                        }

                    }
                }
            }
            else {
                if (!set) {
                    LogManager.println(LogManager.main() + LogManager.args() + " " + arg);
                    cacheManager.addToBuffer("filesOpened", arg);
                    pathArgument = true;
                }
                else {
                    LogManager.println(LogManager.main() + LogManager.args() + " unknown arg: " + arg);
                }

            }
        }
        pluginManager = new PluginManager();
        try {
            if (!nogui) {
                intro = new DoubleHelixAnimation();
            }



            String currentPath = System.getProperty("user.dir");
            File builds = new File(currentPath + "/" + evoalBuildFolder);




            if (!builds.exists() || FileManager.isDirectoryEmpty(builds)) {
                String target = "Downloading EvoAl Build";
                if (!nogui) {
                    intro.setObjective(target);
                }
                else {
                    LogManager.println(target);
                }
                if (nogui) {
                    System.out.println("Downloading evoal build ...");
                };
                Downloader.downloadNewestVersionIfNeeded(false);
                var build = FileManager.findFirstFileInReverseOrder(currentPath + "/" + evoalBuildFolder);
                InputHandler.setEvoAlVersionNoReload(build.getName());
            }

            evoalVersion = cacheManager.getFirstElement(String.class, "build");


            if (evoalVersion == null || !FileManager.folderExists(currentPath + "/" + evoalBuildFolder, evoalVersion)) {
                var build = FileManager.findFirstFileInReverseOrder(currentPath + "/" + evoalBuildFolder);
                InputHandler.setEvoAlVersionNoReload(build.getName());
            }

            String target = "Constructing Syntax-Tree";
            if (!nogui) {
                intro.setObjective(target);
            }
            else {
                LogManager.println(target);
            }


            SyntaxTree.start();
            if (!nogui) {
                intro.stop();
                while (intro.isUnfinished()) {
                    Thread.sleep(100);
                }
            }
        }
        catch (Exception e) {
            throw new Exception(e);
        }

        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void setFont() {
        Font font = new Font("Arial", Font.PLAIN, 14);

        UIManager.put("Button.font", font);
        UIManager.put("ToggleButton.font", font);
        UIManager.put("RadioButton.font", font);
        UIManager.put("CheckBox.font", font);
        UIManager.put("ColorChooser.font", font);
        UIManager.put("ComboBox.font", font);
        UIManager.put("Label.font", font);
        UIManager.put("List.font", font);
        UIManager.put("MenuBar.font", font);
        UIManager.put("MenuItem.font", font);
        UIManager.put("RadioButtonMenuItem.font", font);
        UIManager.put("CheckBoxMenuItem.font", font);
        UIManager.put("Menu.font", font);
        UIManager.put("PopupMenu.font", font);
        UIManager.put("OptionPane.font", font);
        UIManager.put("Panel.font", font);
        UIManager.put("ProgressBar.font", font);
        UIManager.put("ScrollPane.font", font);
        UIManager.put("Viewport.font", font);
        UIManager.put("TabbedPane.font", font);
        UIManager.put("Table.font", font);
        UIManager.put("TableHeader.font", font);
        UIManager.put("TextField.font", font);
        UIManager.put("PasswordField.font", font);
        UIManager.put("TextArea.font", font);
        UIManager.put("TextPane.font", font);
        UIManager.put("EditorPane.font", font);
        UIManager.put("TitledBorder.font", font);
        UIManager.put("ToolBar.font", font);
        UIManager.put("ToolTip.font", font);
        UIManager.put("Tree.font", font);
    }


    static void createAndShowGUI() {
        // Create the main frame
        mainFrame = new JFrame(programName);

        // Set the default close operation to do nothing
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Add a window listener to handle the window closing event
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                InputHandler.tryClose();
            }
        });


        if (os == OS.WINDOWS) {
            mainFrame.setUndecorated(true);
        }
        Image icon = Toolkit.getDefaultToolkit().getImage("imgs/evoal.png");

        // Set the icon image for the frame
        mainFrame.setIconImage(icon);

        // Create and add content to the frame

        borderPanel = new JPanel();

        borderPanel.setLayout(new BorderLayout());


        new Main(); // Initialize main panel

        pluginCreator = new PluginCreator();

        createMenuBar();

        mainPanel.setBackground(Main.bgColor); // Set background color or any layout you need

        // Layout the frame with BorderLayout
        mainFrame.setLayout(new BorderLayout());
        if (os == OS.WINDOWS) {
            borderPanel.add(UiUtil.getHeader(), BorderLayout.NORTH);
        }
        borderPanel.add(mainPanel, BorderLayout.CENTER);
        borderPanel.setBackground(Main.bgColor);
        if (os == OS.WINDOWS) {
            //borderPanel.setBorder(BorderFactory.createEmptyBorder());
            borderPanel.setBorder(new LineBorder(Main.bgColor, 3));

        }else {
            borderPanel.setBorder(BorderFactory.createEmptyBorder());
        }

        mainFrame.add(borderPanel);


        // Set frame size and location
        mainFrame.setSize(new Dimension(800, 600));
        mainFrame.setLocationRelativeTo(null);

        if (nogui) {
            if (!convert) {
                runAndStop();
            }
            else {
                System.exit(0);
            }
        }
        else {
            mainFrame.setVisible(true);
        }

    }

    public Main() {
        UiUtil.adjustMainPanel(this);

        setupUi();

        FileManager.loadRecent();

        postStart();
    }

    private void setupUi() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }


        UiUtil.createConstantPanel();
        UiUtil.createErrorPanel();

        UiUtil.createContentLabel();

        rightPanelTextField = UiUtil.createTextField();
        leftPanelTextField = UiUtil.createTextField();

        UiUtil.createDataPanel();

        UiUtil.createFolderPanel(this);

        UiUtil.setRightPanelTextFieldListeners(this);

        JPanel rightContainerPanel = UiUtil.createRightContainerPanel(this);

        UiUtil.setLeftPanelTextFieldListeners(this);

        JPanel leftContainerPanel = UiUtil.createLeftContainerPanel(this);

        UiUtil.createSplitPanes(this, rightContainerPanel, leftContainerPanel);

        UiUtil.setAdaptersAndListeners(this);

        constantManager.refreshUI();

        UiUtil.setResizer();

        leftPanel.requestFocusInWindow();
        
    }


    public static void postStart() {
        if (!nogui) {
            Thread executionThread = new Thread(() -> {
                try {
                    Downloader.checkForUpdate();
                    try {
                        while (updateChecked()) {
                            Thread.sleep(100);
                        }
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    Downloader.update();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            executionThread.start();
        }
    }


    public static void runAndStop() {
        InputHandler.tryRun();
        while (processRunning) {
            try {
                Thread.sleep(50);

            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        System.exit(0);
    }

}