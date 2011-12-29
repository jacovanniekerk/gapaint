package gj.ea.art;

import gj.ea.art.ga.GASolution;

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

    /**
     * To keep the compiler quiet.
     */
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(ArtMain.class);

    private BufferedImage source;
    private Properties properties;
    
    private Image buffer;
    private Graphics2D gfx;

    private volatile boolean quit;

    private String algorithm;
    private boolean saveImages;

    private EvolutionaryAlgorithm evolutionaryAlgorithm;

    public ArtMain(BufferedImage source, Properties properties) {
        super();
        this.source = source;
        this.properties = properties;

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

    /**
     * Loads an image from disk.
     * 
     * @param fileName
     * @param width
     * @return
     */
    public static BufferedImage getImage(String fileName, int width) {
        try {
            BufferedImage tmp = ImageIO.read(new File(fileName));
            double ratio = tmp.getWidth() / (double) width;
            int height = (int) Math.ceil(tmp.getHeight() / ratio);
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            img.getGraphics().drawImage(tmp, 0, 0, width, height, 0, 0, tmp.getWidth(), tmp.getHeight(), null);
            return img;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void initialise() {
        buffer = this.createImage(this.getWidth(), this.getHeight());
        gfx = (Graphics2D) buffer.getGraphics();
        quit = false;
        
        setParameters(properties);
        evolutionaryAlgorithm = initEvolutionaryAlgorithm(properties);
    }

    private void setParameters(Properties properties) {
        saveImages = Boolean.parseBoolean(properties.getProperty("saveImages", "true"));
        algorithm = properties.getProperty("algorithm", "true");
    }

    @SuppressWarnings("unchecked")
    private EvolutionaryAlgorithm initEvolutionaryAlgorithm(Properties properties) {
        try {
            Class<EvolutionaryAlgorithm> clazz = (Class<EvolutionaryAlgorithm>) Class.forName(algorithm);
            EvolutionaryAlgorithm ea = clazz.newInstance();
            ea.initialise(source, properties);
            logger.debug("You selected the " + clazz.getCanonicalName() + " strategy.");
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

    public void go() throws FileNotFoundException {

        // Draws the source image on the left side for comparison.
        gfx.drawImage(source, 0, 0, null);

        long bestOfTheBest = Long.MAX_VALUE;

        // To allow graphing, etc.
        PrintWriter csv = new PrintWriter(new FileOutputStream(new File("gachanges.csv"), true));

        while (!evolutionaryAlgorithm.stoppingConditionMet() && !quit) {
            evolutionaryAlgorithm.iterate();
            GASolution best = (GASolution) evolutionaryAlgorithm.getBestSolution();

            logger.debug(evolutionaryAlgorithm.getProgressString());
            if (best != null) {
                gfx.drawImage(best.getScreenSolutionImage(), source.getWidth(), 0, null);
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

            // Apparently wait() can also work.
            try {
                Thread.sleep(1L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            
        }
        csv.close();

        repaint();
    }

    @Override
    public int getWidth() {
        return source.getWidth() * 2;
    }

    @Override
    public int getHeight() {
        return source.getHeight();
    }

    public void paint(Graphics gc) {
        if (buffer != null) {
            gc.drawImage(buffer, 0, 0, null);
        }
    }

    private static void usage() {
        System.out.println("Evolving art application.\nby Jaco van Niekerk.");
        System.out.println("\nUsage: ./go.sh <image to evolve> [<width>].");
        System.out.println("\nNote, the height will be determined from the aspect ratio, given the width.  The default image");
        System.out.println("is tux, and if the width is omitted, 500 will be used.\n\nHave fun!");
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Evolving  Paintings using Evolutionary Algorithms");

        if (args.length != 0 && args[0].equals("-h")) {
            usage();
        }

        // Get the source image (or the default if none specified).
        String fileName = args.length > 0 ? args[0] : ArtMain.class.getResource("tux.jpg").getFile();
        String size = args.length > 1 ? args[1] : "300";
        BufferedImage pic = ArtMain.getImage(fileName, Integer.parseInt(size));

        InputStream inputStreamForProperties = ArtMain.class.getResourceAsStream("ga.properties");
        Properties properties = new Properties();

        
        try {
            properties.load(inputStreamForProperties);
            
            ArtMain artMain = new ArtMain(pic, properties);
            
            frame.getContentPane().setPreferredSize(new Dimension(artMain.getWidth(), artMain.getHeight()));
            frame.getContentPane().setLayout(new BorderLayout());
            frame.getContentPane().add(artMain, BorderLayout.CENTER);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);

            artMain.setFocusable(true);
            artMain.requestFocus();

            artMain.initialise();
            artMain.go();
        } catch (IOException e) {
            logger.error("Ok, bad news. The algorithm cannot start", e);
        }

    }

}
