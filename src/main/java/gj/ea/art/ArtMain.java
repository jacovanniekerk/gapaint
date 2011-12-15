package gj.ea.art;

import gj.ea.art.ga.GA;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * The main class that links everything together.
 * 
 * TODO Needs proper interface which will enable selection, parameter tweaking,
 *       etc.
 * 
 * @author jaco
 * 
 */
public class ArtMain extends JPanel {

    /**
     * To keep the compiler quiet. 
     */
    private static final long serialVersionUID = 1L;
    
    private Image source;
    private Image buffer;
    private Graphics2D gfx;
    private boolean quit = false;

    public ArtMain(Image image) {
        super();
        this.source = image;

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
    private static Image getImage(String fileName, int width) {
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
    
    public void init() {
        buffer = this.createImage(this.getWidth(), this.getHeight());
        gfx = (Graphics2D) buffer.getGraphics();
    }

    public void go(EvolutionaryAlgorithm algorithm, boolean saveImages) {
        //algorithm.initialise(source, properties);
        gfx.drawImage(source, 0, 0, null);
        
        // ea here...
        // ...
        
        repaint();
    }
    
    @Override
    public int getWidth() {
        return source.getWidth(null) * 2;
    }

    @Override
    public int getHeight() {
        return source.getHeight(null);
    }

    public void paint(Graphics gc) {
        if (buffer != null) {
            gc.drawImage(buffer, 0, 0, null);
        }
    }

    private static EvolutionaryAlgorithm initGA(InputStream ga) throws IOException {
        Properties prop = new Properties();
        prop.load(ga);
        
        System.out.println(prop.getProperty("blah"));
        
        return null;
    }
    
    private static void usage() {
        System.out.println("Evolving art application.\nby Jaco van Niekerk.");
        System.out.println("\nUsage: ./go.sh <image to evolve> [<width>].");
        System.out.println("\nNote, the height will be determined from the aspect ratio, given the width.  The default image");
        System.out.println("is tux, and if the width is omitted, 500 will be used.\n\nHave fun!");
    }
    
    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Evolving  aintings using Evolutionary Algorithms");

        if (args.length != 0 && args[0].equals("-h")) {
            usage();
        }
        
        String fileName = args.length > 0 ? args[0] : ArtMain.class.getResource("tux.jpg").getFile();
        String size = args.length > 1 ? args[1] : "300";

        Image pic = ArtMain.getImage(fileName, Integer.parseInt(size));

        ArtMain ea = new ArtMain(pic);

        frame.getContentPane().setPreferredSize(new Dimension(ea.getWidth(), ea.getHeight()));
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(ea, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        ea.setFocusable(true);
        ea.requestFocus();
        
        ea.init();
        
        EvolutionaryAlgorithm ga = initGA(ArtMain.class.getResourceAsStream("ga.properties"));
        
        ea.go(ga, true);
    }

}
