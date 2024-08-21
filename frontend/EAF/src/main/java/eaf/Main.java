package eaf;

import eaf.compiler.SyntaxTree;
import eaf.intro.DoubleHelixAnimation;
import eaf.manager.CacheManager;
import eaf.manager.ExtraRectManager;
import eaf.models.ClassType;
import eaf.ui.*;
import eaf.manager.FileManager;
import eaf.ui.panels.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static eaf.ui.UiUtil.showUnsaveDialog;

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

    public static ConstantPane constantManager;

    public static ErrorPane errorManager;

    private static final int RECT_SPACING = 5;
    public final RectPanel leftPanel = new RectPanel();
    public final RectPanel rightPanel = new RectPanel();

    public static int arrayDefaultCount = 1;

    public JSplitPane mainSplitPane = null;

    public static Main mainPanel = null;

    public static DataFieldListPane dataPanel = null;

    public static JFrame mainFrame = null;

    // Declare the text field and new panel
    public JTextField rightPanelTextField;
    public JTextField leftPanelTextField;
    public JPanel rightContainerPanel;
    public FolderPanel folderPanel;

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
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                var cancle = showUnsaveDialog();
                if (!cancle) {
                    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                } else {
                    mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                }
            }

        });


        UiUtil.createMenuBar();

        // Create and add content to the frame
        new Main();
        mainFrame.add(mainPanel);

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

        leftPanel.requestFocusInWindow();
        
    }





}