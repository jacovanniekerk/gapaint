package gj.ea.art.ga;

import gj.ea.art.ArtSolution;
import gj.ea.art.helpers.FitnessHelper;
import gj.ea.art.helpers.RenderImageHelper;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Represents a given solution.
 * 
 * @author Jaco van Niekerk
 * 
 */
public class GASolution implements ArtSolution, Serializable {

    private static final long serialVersionUID = 1L;

    // Parameters as passed in via constructor.
    private int polygonCount;
    private int polyVertexCount;

    private double mutateModifyChance;
    private double mutateDormantChance;
    private double mutateRearrengeChance;

    // Housekeeping.
    private BufferedImage sourceImage; // what we want
    private Properties properties;

    // Solution-specific values (including "genes")
    private Polygon[] polys;
    private Color[] cols;
    
    private transient Future<BufferedImage> target; // current result
    private transient Future<Long> fitness; // fitness (target against sourceImage)

    public GASolution(BufferedImage sourceImage, Properties properties) {
        this.sourceImage = sourceImage;
        this.properties = properties;
        setParameters(properties);
    }

    private void setParameters(Properties properties) {
        polygonCount = Integer.parseInt(properties.getProperty("polygonCount", "500"));
        polyVertexCount = Integer.parseInt(properties.getProperty("polyVertexCount", "5"));

        mutateModifyChance = Double.parseDouble(properties.getProperty("mutateModifyChance", "0.01"));
        mutateDormantChance = Double.parseDouble(properties.getProperty("mutateDormantChance", "0.005"));
        mutateRearrengeChance = Double.parseDouble(properties.getProperty("mutateRearrengeChance", "0.008"));
    }

    private Polygon getRandomPoly() {
        Polygon polygon = new Polygon();
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();

        Point p = new Point((int) (Math.random() * width), (int) (Math.random() * height));
        for (int i = 0; i < polyVertexCount; i++) {
            polygon.addPoint((int) p.getX() + (int)(10*Math.random()), (int) p.getY() + (int)(10*Math.random()));
        }
        return polygon;
    }

    private Color getRandomColor(float alpha) {
        return new Color((float) Math.random(), (float) Math.random(), (float) Math.random(), alpha);
    }
    
    private Color getRandomColor() {
        return getRandomColor((float) Math.random());
    }

    /**
     * This creates a random individual and calls validate() to render the
     * individual and calculate its fitness.
     */
    public void initialise() {
        polys = new Polygon[polygonCount];
        cols = new Color[polygonCount];
        
        for (int i = 0; i < polygonCount; i++) {
            polys[i] = getRandomPoly();
            cols[i] = getRandomColor(1);
        }

        validate();
    }

    /**
     * This method must be called once the new child has been created. If it is
     * not explicitly called, it will be called once the fitness or the target
     * image is requested. This will cause the algorithm to block until both are
     * available.
     * 
     * This method should also be called explicitly after object
     * deserialisation.
     */
    private void validate() {
        target = RenderImageHelper.submitNewRenderTask(polys, cols, polygonCount, sourceImage.getWidth(), sourceImage.getHeight(), true);
        fitness = FitnessHelper.submitNewFitnessTask(this.sourceImage, target);
    }

    @Override
    public long getFitness() {
        if (fitness == null) {
            validate();
        }
        return FitnessHelper.waitForFitness(fitness);
    }

    @Override
    public BufferedImage getScreenSolutionImage() {
        if (target == null) {
            validate();
        }
        return RenderImageHelper.waitForImage(target);
    }

    @Override
    public BufferedImage getDiskSolutionImage() {
        return getScreenSolutionImage();
    }

    public GASolution mate(GASolution mate) {
        GASolution offspring = this.crossover(mate);
        offspring.mutate();
        offspring.validate();
        return offspring; // and a child is born...
    }
    
    private GASolution crossover(GASolution mate) {
        int singlePoint = (int) (Math.random() * polygonCount);
        GASolution offspring = new GASolution(sourceImage, properties);

        Polygon[] childPoly = new Polygon[polygonCount];
        Color[] childCol = new Color[polygonCount];

        // Copy first set of genes from the first parent.
        for (int i = 0; i < singlePoint; i++) {
            childPoly[i] = new Polygon();
            for (int j = 0; j < polys[i].npoints; j++)
                childPoly[i].addPoint(polys[i].xpoints[j], polys[i].ypoints[j]);
            childCol[i] = new Color(cols[i].getRed(), cols[i].getGreen(), cols[i].getBlue(), cols[i].getAlpha());
        }
        // Copy second set from second parent.
        for (int i = singlePoint; i < polygonCount; i++) {
            childPoly[i] = new Polygon();
            for (int j = 0; j < mate.polys[i].npoints; j++)
                childPoly[i].addPoint(mate.polys[i].xpoints[j], mate.polys[i].ypoints[j]);
            childCol[i] = new Color(mate.cols[i].getRed(), mate.cols[i].getGreen(), mate.cols[i].getBlue(), mate.cols[i].getAlpha());
        }
        // Pass "genes" to offspring.
        offspring.polys = childPoly;
        offspring.cols = childCol;

        return offspring; 
    }

    /**
     * Make one or more genes dormant.
     * 
     */
    private void mutateMakeGenesDormant() {
        // TODO It makes more sense to have the dormant chance directly
        // proportional to how early the the gene (poly) gets placed. We also
        // need a "reassignment" operator to capitalise further on this,
        // increasing exploration later in the process.
        
        while (Math.random() < mutateDormantChance) {
            int which = (int) (Math.random() * polygonCount);
            cols[which] = new Color(cols[which].getRed(), cols[which].getGreen(), cols[which].getBlue(), 0);
        }
    }

    /**
     * Rearrange some of the genes in the genome.
     */
    private void mutateRearrange() {
        while (Math.random() < mutateRearrengeChance) {
            int a = (int) (Math.random() * polygonCount);
            int b = (a + (int) (Math.random() * polygonCount)) % polygonCount;
            Polygon poly = polys[a];
            Color col = cols[a];
            polys[a] = polys[b];
            cols[a] = cols[b];
            polys[b] = poly;
            cols[b] = col;
        }
    }

    private static double distribution(int max) {
        double delta = Math.random() * Math.random() * max;
        return Math.random() > 0.5 ? delta : -delta;
    }

    /**
     * Changes a component of random individuals.
     */
    private void mutateChance() {
        final int COLOUR_EXTENT_CHANGE = 255;
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        while (Math.random() < mutateModifyChance) {
            int which = (int) (Math.random() * polygonCount);
            int component = (int) (Math.random() * (polyVertexCount + 1));
            if (component == polyVertexCount) { // change the colour
                int red = Math.min(Math.max(cols[which].getRed() + (int) distribution(COLOUR_EXTENT_CHANGE), 0), 255);
                int green = Math.min(Math.max(cols[which].getGreen() + (int) distribution(COLOUR_EXTENT_CHANGE), 0), 255);
                int blue = Math.min(Math.max(cols[which].getBlue() + (int) distribution(COLOUR_EXTENT_CHANGE), 0), 255);
                int alpha = Math.min(Math.max(cols[which].getAlpha() + (int) distribution(COLOUR_EXTENT_CHANGE), 0), 255);
                cols[which] = new Color(red, green, blue, alpha);
            } else { // change something else
                polys[which].xpoints[component] = polys[which].xpoints[component] + (int) distribution(width);
                polys[which].ypoints[component] = polys[which].ypoints[component] + (int) distribution(height);
            }
        }
    }

    /**
     * Calls the three mutation operations on the current child.
     */
    private void mutate() {
        mutateMakeGenesDormant();
        // reassignGenes();
        mutateRearrange();
        mutateChance();
    }

}
