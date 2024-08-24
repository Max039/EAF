package eaf;

import eaf.compiler.SyntaxTree;
import eaf.intro.DoubleHelixAnimation;
import eaf.manager.CacheManager;
import eaf.models.Pair;
import eaf.ui.*;
import eaf.manager.FileManager;
import eaf.ui.panels.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;

import static eaf.ui.UiUtil.createMainTab;
import static eaf.ui.UiUtil.createMenuBar;

public class Main extends JPanel {

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
    public static String evoalVersion = "20240708-152016";

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

    public static String programName = "EAF";

    public static CacheManager cacheManager = new CacheManager();

    public static DoubleHelixAnimation animation = null;

    public static ArrayList<Pair<String, JComponent>> tabsList;

    public static ArrayList<JButton> tabButtons;

    public static void main(String[] args) throws Exception {
        try {
            animation = DoubleHelixAnimation.create();
            animation.objective = "Constructing Syntax-Tree";
            SyntaxTree.start();
            animation.stop();
            while (animation.isUnfinished()) {
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

        mainFrame.setUndecorated(true);
        Image icon = Toolkit.getDefaultToolkit().getImage("imgs/evoal.png");

        // Set the icon image for the frame
        mainFrame.setIconImage(icon);

        // Create and add content to the frame

        borderPanel = new JPanel();

        borderPanel.setLayout(new BorderLayout());

        new Main(); // Initialize main panel


        createMenuBar();

        mainPanel.setBackground(Color.LIGHT_GRAY); // Set background color or any layout you need

        // Layout the frame with BorderLayout
        mainFrame.setLayout(new BorderLayout());
        borderPanel.add(UiUtil.getHeader(), BorderLayout.NORTH);
        borderPanel.add(mainPanel, BorderLayout.CENTER);

        borderPanel.setBorder(new LineBorder(Main.bgColor, 3));
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
    }

    private void setupUi() {
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


}