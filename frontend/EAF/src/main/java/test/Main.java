package test;

import compiler.SyntaxTree;
import intro.DoubleHelixAnimation;

import javax.swing.*;
import java.awt.*;

public class Main extends JPanel {

    public static Color bgColor = new Color(49, 51, 53);

    public static Color dividerColor = new Color(35, 35, 35);

    public static Color scrollBarBg = new Color(43, 43, 43);

    public static Color searchBarError = new Color(200, 100, 100);

    public static Color scrollBarButton = new Color(150, 150, 150);

    public static Color scrollBarTopAndBottom = new Color(60, 60, 60);

    public static Color searchBarText = new Color(255, 255, 255);

    public static Color searchBar = new Color(100, 100, 100);
    public static Color searchBarBorder = new Color(85, 85, 85);

    private static final int RECT_SPACING = 5;
    public final RectPanel leftPanel = new RectPanel();
    public final RectPanel rightPanel = new RectPanel() ;

    public static int arrayDefaultCount = 1;

    public JSplitPane mainSplitPane = null;

    public static Main mainPanel = null;

    public static DataFieldListPane dataPanel = null;

    public static JFrame mainFrame = null;

    // Declare the text field and new panel
    JTextField rightPanelTextField;
    public JTextField leftPanelTextField;
    JPanel rightContainerPanel;
    FolderPanel folderPanel;

    public static String saveFormat = "eaf";

    public static String savesPath = "/saves";

    public static CacheManager cacheManager = new CacheManager();

    
    public static void main(String[] args) throws Exception {
        try {
            var s = DoubleHelixAnimation.create();
            s.objective = "Constructing Syntax-Tree";
            SyntaxTree.start();
            s.stop();
            while (s.isUnfinished()) {
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
        mainFrame = new JFrame("EAF");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GuiCreator.createMenuBar();

        // Create and add content to the frame
        new Main();
        mainFrame.add(mainPanel);

        // Set frame size and location
        mainFrame.setSize(new Dimension(800, 600));
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    public Main() {
        GuiCreator.adjustMainPanel(this);

        setupUi();

        FileManager.loadRecent();
    }

    private void setupUi() {
        GuiCreator.createContentLabel();

        rightPanelTextField = GuiCreator.createTextField();
        leftPanelTextField = GuiCreator.createTextField();

        GuiCreator.createDataPanel();

        GuiCreator.createFolderPanel(this);

        GuiCreator.setRightPanelTextFieldListeners(this);

        JPanel rightContainerPanel = GuiCreator.createRightContainerPanel(this);

        GuiCreator.setLeftPanelTextFieldListeners(this);

        JPanel leftContainerPanel = GuiCreator.createLeftContainerPanel(this);

        GuiCreator.createSplitPanes(this, rightContainerPanel, leftContainerPanel);

        GuiCreator.setAdaptersAndListeners(this);
    }





}