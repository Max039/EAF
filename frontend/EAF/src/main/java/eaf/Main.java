package eaf;

import eaf.compiler.SyntaxTree;
import eaf.download.Downloader;
import eaf.executor.OpenIntelliJProject;
import eaf.input.InputHandler;
import eaf.intro.DoubleHelixAnimation;
import eaf.intro.Intro;
import eaf.intro.SimpleIntro;
import eaf.manager.CacheManager;
import eaf.models.Pair;
import eaf.plugin.PluginCreator;
import eaf.plugin.PluginManager;
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

    public static String version = "0.1.0";

    public static Color bgColor = new Color(49, 51, 53);

    public static Color dividerColor = new Color(35, 35, 35);

    public static Color scrollBarBg = new Color(43, 43, 43);

    public static Color searchBarError = new Color(200, 100, 100);

    public static Color scrollBarButton = new Color(150, 150, 150);

    public static Color scrollBarTopAndBottom = new Color(60, 60, 60);

    public static Color searchBarText = new Color(255, 255, 255);

    public static Color searchBar = new Color(100, 100, 100);
    public static Color searchBarBorder = new Color(85, 85, 85);

    public static ConstantPane constantManager;

    public static ErrorPane errorManager;

    private static final int RECT_SPACING = 5;
    //public static String evoalVersion = "20240708-152016";
    public static String evoalVersion = null;

    public final RectPanel leftPanel = new RectPanel();
    public final RectPanel rightPanel = new RectPanel();

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

        if (os == OS.MAC) {
            FileManager.copyToDocuments();
        }

        cacheManager = new CacheManager();
        System.out.println("Args:");
        int i = 0;
        for (var arg : args) {
            if (i == 0) {
                System.out.println(arg);
                cacheManager.addToBuffer("filesOpened", arg);
            }
            i++;
        }
        pluginManager = new PluginManager();
        try {
            if (os == OS.WINDOWS) {
                intro = new DoubleHelixAnimation();
            }
            else {
                intro = new SimpleIntro();
            }

            String currentPath = System.getProperty("user.dir");
            File builds = new File(currentPath + "/" + evoalBuildFolder);




            if (!builds.exists() || FileManager.isDirectoryEmpty(builds)) {
                intro.setObjective("Downloading EvoAl Build");
                Downloader.downloadNewestVersionIfNeeded(false);
                var build = FileManager.findFirstFileInReverseOrder(currentPath + "/" + evoalBuildFolder);
                InputHandler.setEvoAlVersionNoReload(build.getName());
            }

            evoalVersion = cacheManager.getFirstElement(String.class, "build");


            if (evoalVersion == null || !FileManager.folderExists(currentPath + "/" + evoalBuildFolder, evoalVersion)) {
                var build = FileManager.findFirstFileInReverseOrder(currentPath + "/" + evoalBuildFolder);
                InputHandler.setEvoAlVersionNoReload(build.getName());
            }

            intro.setObjective("Constructing Syntax-Tree");
            SyntaxTree.start();
            intro.stop();
            while (intro.isUnfinished()) {
                Thread.sleep(100);
            }
        }
        catch (Exception e) {
            throw new Exception(e);
        }

        SwingUtilities.invokeLater(() -> createAndShowGUI());
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

        mainPanel.setBackground(Color.LIGHT_GRAY); // Set background color or any layout you need

        // Layout the frame with BorderLayout
        mainFrame.setLayout(new BorderLayout());
        if (os == OS.WINDOWS) {
            borderPanel.add(UiUtil.getHeader(), BorderLayout.NORTH);
        }
        borderPanel.add(mainPanel, BorderLayout.CENTER);

        if (os == OS.WINDOWS) {
            borderPanel.setBorder(new LineBorder(Main.bgColor, 3));
        }else {
            borderPanel.setBorder(BorderFactory.createEmptyBorder());
        }

        mainFrame.add(borderPanel);


        // Set frame size and location
        mainFrame.setSize(new Dimension(800, 600));
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
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