package gj.ea.art;

import gj.ea.art.ga.GASolution;
import gj.ea.art.helpers.PersistenceHelper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

/**
 * The main class that links everything together.
 * 
 * TODO Needs proper interface which will enable selection, parameter tweaking,
 * etc.
 * 
 * @author jaco
 * 
 */
public class ArtMain extends JPanel {

    public class DrawPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        private Image buffer;
        private Graphics2D gfx;
        private BufferedImage source;
        
        public DrawPanel(BufferedImage source) {
            super();
            this.source = source;
            this.setFocusable(false);
        }
        
        public void init() {
            buffer = this.createImage(this.getWidth(), this.getHeight());
            gfx = (Graphics2D) buffer.getGraphics();
        }
        
        @Override
        public void paint(Graphics gc) {
            if (buffer != null) {
                gc.drawImage(buffer, 0, 0, null);
            }
        }
        
        @Override
        public void update(Graphics gc) {
            paint(gc);
        }
        
        public Graphics2D getGraphics2D() {
            return gfx;
        }
        
        @Override
        public int getWidth() {
            return source.getWidth() * 2;
        }

        @Override
        public int getHeight() {
            return source.getHeight();
        }
    }

    // ~
  
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(ArtMain.class);

    private JTextField statusBar;
    private DrawPanel drawPanel; 
    
    private String sourceImageFileName;
    private BufferedImage sourceImage;
    private int imageSize;
    private Properties properties;
    
    private volatile boolean quit;

    // Properties.
    private String algorithm;
    private boolean saveImages;
    //private int imageSize;
    
    private EvolutionaryAlgorithm evolutionaryAlgorithm;

    public ArtMain(String sourceImageFileName, Properties properties) {
        super();
        this.sourceImageFileName = sourceImageFileName;
        this.properties = properties;
        setParameters(properties);
        sourceImage = PersistenceHelper.getImage(sourceImageFileName, imageSize);
        drawPanel = new DrawPanel(sourceImage);
        statusBar = new JTextField("Evolving Paintings using Evolutionary Algorithms");
        
        this.setLayout(new BorderLayout());
        this.add(statusBar, BorderLayout.SOUTH);
        this.add(drawPanel, BorderLayout.CENTER);
        
        this.requestFocus();
        
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                // for graceful termination
                if (Character.toLowerCase(e.getKeyChar()) == 'q') {
                    quit = true;
                }
            }
        });
    }

    public void initialise() {
        quit = false;
        
        
        evolutionaryAlgorithm = initEvolutionaryAlgorithm(properties);
        
        drawPanel.init();
    }

    private void setParameters(Properties properties) {
        algorithm = properties.getProperty("algorithm", "true");
        saveImages = Boolean.parseBoolean(properties.getProperty("saveImages", "true"));
        imageSize = Integer.parseInt(properties.getProperty("imageSize", "300"));
    }
    
    @SuppressWarnings("unchecked")
    private EvolutionaryAlgorithm createNew(Properties properties) {
        Class<EvolutionaryAlgorithm> clazz;
        try {
            clazz = (Class<EvolutionaryAlgorithm>) Class.forName(algorithm);
            logger.debug("You selected the " + clazz.getCanonicalName() + " strategy.");
            EvolutionaryAlgorithm ea = clazz.newInstance();
            ea.initialise(sourceImageFileName, properties);
            return ea;
        } catch (ClassNotFoundException e) {
            logger.error("I could not find the EA strategy (" + algorithm + " you wanted.)");
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            logger.error("I could not instantiate the EA class (" + algorithm + ".)");
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            logger.error("The EA class (" + algorithm + ") is badly defined and I got an IllegalAccessException.");
            throw new RuntimeException(e);
        }
    }
        
    private EvolutionaryAlgorithm initEvolutionaryAlgorithm(Properties properties) {
        if (new File("eastate").exists()) {
            logger.debug("I found a 'eastate' file, attempting to load...");
            EvolutionaryAlgorithm ea = PersistenceHelper.loadState("eastate");
            logger.debug("The saved state utilised the " + ea.getClass().getCanonicalName() + " strategy.");
            return ea;
        } else {
            logger.debug("I did not find any 'eastate' file, creating a new EA, based on defined properties.");
            return createNew(properties);
        }
    }

    public void go() throws FileNotFoundException {

        // Draws the source image on the left side for comparison.
        drawPanel.getGraphics2D().drawImage(sourceImage, 0, 0, null);

        long bestOfTheBest = Long.MAX_VALUE;

        // To allow graphing, etc.
        PrintWriter csv = new PrintWriter(new FileOutputStream(new File("gachanges.csv"), true));

        while (!evolutionaryAlgorithm.stoppingConditionMet() && !quit) {
            evolutionaryAlgorithm.iterate();
            GASolution best = (GASolution) evolutionaryAlgorithm.getBestSolution();

            logger.debug(evolutionaryAlgorithm.getProgressString());
            statusBar.setText(evolutionaryAlgorithm.getProgressString());
            if (best != null) {
                drawPanel.getGraphics2D().drawImage(best.getScreenSolutionImage(), sourceImage.getWidth(), 0, null);
                csv.append(evolutionaryAlgorithm.getGenerationCounter() + ", " + best.getFitness() + "\n");
                csv.flush();

                if (saveImages && best.getFitness() < bestOfTheBest) {
                    bestOfTheBest = best.getFitness();
                    try {
                        ImageIO.write(best.getDiskSolutionImage(), "png", new File("ga_" + System.currentTimeMillis() + ".png"));
                    } catch (Exception e) {
                        logger.error("The ouput PNG could not be written!", e);
                    }
                }
            }

            repaint();

           try {
                Thread.sleep(1L); // Still looking for a better method than this...
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            
        }
        statusBar.setText("Process stopped... (you can now close this window if you wish.)");
        csv.close();
        repaint();
    }

    @Override
    public int getWidth() {
        return drawPanel.getWidth();
    }

    @Override
    public int getHeight() {
        return drawPanel.getHeight() + statusBar.getHeight();
    }

    private static void usage() {
        System.out.println("Evolving art application.\ndevelopers: Jaco van Niekerk, Nico Kruger.");
        System.out.println("\nUsage: ./go.sh <image to evolve> [<width>].");
        System.out.println("\nNote, the height will be determined from the aspect ratio, given the width (default is 500).\n\nHave fun!");
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Evolving Paintings using Evolutionary Algorithms");

        if (args.length != 0 && args[0].equals("-h")) {
            usage();
        }

        InputStream inputStreamForProperties = ArtMain.class.getResourceAsStream("ga.properties");
        Properties properties = new Properties();
        
        try {
            properties.load(inputStreamForProperties);

            if (args.length == 0) {
                usage();
                return;
            }

            ArtMain artMain = new ArtMain(args[0], properties);
            
            //JTextField test = new JTextField("Testing...");
            //test.setEditable(false);
            frame.getContentPane().setLayout(new BorderLayout());
            //frame.getContentPane().add(test, BorderLayout.SOUTH);
            frame.getContentPane().add(artMain, BorderLayout.CENTER);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            artMain.setFocusable(true);
            artMain.requestFocus();
            
            frame.setVisible(true);
            frame.setResizable(false);
            frame.getContentPane().setPreferredSize(new Dimension(artMain.getWidth(), artMain.getHeight()));
            frame.pack();

            artMain.initialise();
            artMain.go();
        } catch (IOException e) {
            logger.error("Ok, bad news. The algorithm cannot start", e);
        }

    }

}
