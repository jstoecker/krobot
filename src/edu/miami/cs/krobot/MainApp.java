package edu.miami.cs.krobot;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import edu.miami.cs.krobot.model.Model;
import edu.miami.cs.krobot.ui.JointControlPanel;
import edu.miami.cs.krobot.ui.MainMenu;
import edu.miami.cs.krobot.ui.MotionPanel;
import edu.miami.cs.krobot.view.RobotView;

public class MainApp {

  public static String         ROOT_DIR;

  public static void main(String[] args) {

    if (args.length != 1 && args.length != 2) {
      System.out.println("Usage: ./kRobot <model> <config>");
      System.out.println("<model>  = path to robot model YAML file");
      System.out.println("<config> = (optional) path to a kRobot config YAML file");
      return;
    }

    ROOT_DIR = System.getProperty("krobot.rootdir");
    if (ROOT_DIR == null) ROOT_DIR = System.getProperty("user.dir");

    String configFileName = (args.length == 1) ? "config.yml" : args[1];
    File configFile = MainApp.getFile(configFileName);
    if (configFile == null) {
      System.out.println("Error finding config file \"" + configFileName + "\" -- aborting.");
      return;
    }
    
    Config config = Config.load(configFile);
    if (config == null) {
      System.out.println("Error reading config file \"" + args[1] + "\" -- aborting.");
      return;
    }
    
    Model model = Model.loadFromYAML(getFile(args[0]));
    if (model == null) {
      System.out.println("Error loading model \"" + args[0] + "\" -- aborting.");
      return;
    }

    GLCapabilities glc = new GLCapabilities(GLProfile.get("GL2"));
    glc.setSampleBuffers(config.msaaEnabled);
    glc.setNumSamples(config.msaaSamples);
    final GLCanvas canvas = new GLCanvas(glc);

    final MainControl mainControl = new MainControl(model, canvas, config);

    JFrame window = new JFrame("kRobot");
    window.setLayout(new BorderLayout());
    
    window.add(canvas, BorderLayout.CENTER);
    window.setSize(500, 600);
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        mainControl.shutdown();
      }
    });

    MainMenu mainMenu = new MainMenu(model, mainControl, config);
    window.setJMenuBar(mainMenu);

    window.setVisible(true);
    
    JPanel sidePanel = new JPanel();
//    sidePanel.setPreferredSize(new Dimension(400,0));
    JTabbedPane tp = new JTabbedPane();
    sidePanel.add(tp);
    tp.add("Motion", new MotionPanel(config, model, mainControl));
    tp.add("Joints",
        new JointControlPanel(model, (RobotView) mainControl.getGlViews().get("Robot View")));
//    tp.add("Meshes",
//        new MeshControlPanel(model, (RobotView) mainControl.getGlViews().get("Robot View")));
    
    window.add(new JScrollPane(sidePanel), BorderLayout.WEST);
    window.pack();
    window.setLocationRelativeTo(null);
  }

  /**
   * Retrieves a file using the krobot.rootdir property if it is set; otherwise, looks in the
   * current directory.
   * 
   * @param name - name of the file relative to the krobot.rootdir or current directory
   * @return - the file if it exists; null otherwise
   */
  public static File getFile(String name) {
    File file = new File(ROOT_DIR, name);
    if (!file.exists()) {
      file = new File(name);
    }

    return file.exists() ? file : null;
  }
}
