package gj.ea.art.helpers;

import gj.ea.art.EvolutionaryAlgorithm;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

public final class PersistenceHelper {

    private static final Logger logger = Logger.getLogger(PersistenceHelper.class);
    
    private PersistenceHelper() {}
    
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
    
    public static void saveState(EvolutionaryAlgorithm ea, String filename) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
            out.writeObject(ea);
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static EvolutionaryAlgorithm loadState(String filename) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
            EvolutionaryAlgorithm ea = (EvolutionaryAlgorithm) in.readObject();
            in.close();
            return ea;
        } catch (Exception e) {
            logger.debug("I could not load the old 'eastate' file.  Sorry, the algorithm has to \n" +
            		"restart.  Delete the " + filename + " file and try again.");
        }
        return null;
    }
    
}
