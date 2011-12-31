package gj.ea.art.ga;

import gj.ea.art.ArtSolution;
import gj.ea.art.EvolutionaryAlgorithm;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;

public class GA implements EvolutionaryAlgorithm, Serializable {

    private static final long serialVersionUID = 1L;

    //private static final Logger logger = Logger.getLogger(GA.class);
    
    // Parameters as passed in via constructor
    private int populationSize;

    // GA values
    private GASolution[] population;
    private long timeSpent; // the combined time in milliseconds spent.
    private int generationCounter; // the number of generations.
    private long previousFitness; // previous iteration's fitness.
    private int lastImprovement; // the number of generations ago that a fitness was witness.
    private String feedback; // a feedback string.

    /*private static void saveState(GA ga, String filename) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
            out.writeObject(ga);
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static GA loadState(String filename) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
            GA ga = (GA) in.readObject();
            in.close();
            return ga;
        } catch (Exception e) {
            logger.debug("I could not load the old 'gastate' file.  Sorry, the algorithm has to \nrestart.  Delete the 'gastate' file and try again.");
        }
        return null;
    }

    public static GA createNewGeneticAlgorithm(BufferedImage sourceImage, Properties properties) {
        if (new File("gastate").exists()) {
            GA ga = loadState("gastate");
            if (ga != null) {
                ga.setParameters(properties);
                return ga;
            }
        }
        GA ga = new GA();
        ga.initialiseNewRun(sourceImage, properties);
        ga.setParameters(properties);
        return ga;
    }*/
    
    @Override
    public void initialise(BufferedImage sourceImage, Properties properties) {
        
        setParameters(properties);
        
        population = new GASolution[populationSize];
        
        timeSpent = 0;
        generationCounter = 0;
        previousFitness = -1;
        lastImprovement = -1;
        feedback = "I have not started yet!";
        
        for (int i = 0; i < population.length; i++) {
            population[i] = new GASolution(sourceImage, properties);
            population[i].initialise();
        }
        sort(population, true);
    }

    public static void sort(GASolution[] solutions, final boolean ascending) {
        Arrays.sort(solutions, new Comparator<GASolution>() {
            @Override
            public int compare(GASolution o1, GASolution o2) {
                int value = ascending ? 1 : -1;
                if (o1.getFitness() > o2.getFitness())
                    return value;
                else if (o2.getFitness() > o1.getFitness())
                    return -value;
                else
                    return 0;
            }
        });
    }

    private void setParameters(Properties properties) {
        populationSize = Integer.parseInt(properties.getProperty("populationSize", "100"));
    }

    @Override
    public ArtSolution getBestSolution() {
        return population[0];
    }

    @Override
    public void iterate() {
        
        // Start the stop-watch.
        long stopWatch = System.currentTimeMillis();
        
        // Temporary array to keep both the offspring and the parents.
        GASolution[] nextPopulation = new GASolution[populationSize * 2];
        for (int i = 0; i < population.length; i++) {
            // Elegant way to ensure that mate != i (this avoids the nasty while-loop).
            int mate = (i + (int) (Math.random() * Math.random() * population.length)) % population.length;
            nextPopulation[i] = population[i].mate(population[mate]);
        }
        
        // Copy parents to new generation and copy the best individuals (i.e. children/parents compete).
        System.arraycopy(population, 0, nextPopulation, population.length, population.length);
        sort(nextPopulation, true);
        System.arraycopy(nextPopulation, 0, population, 0, population.length);

        long delta = 0;
        if (population[0].getFitness() < previousFitness || previousFitness == -1) {
            delta = previousFitness - population[0].getFitness();
            previousFitness = population[0].getFitness();
            lastImprovement = 0;
            // saveState("gastate");
        }
        
        // Update GA variables.
        lastImprovement++;
        generationCounter++;
        timeSpent = timeSpent + (System.currentTimeMillis() - stopWatch);
        
        // Construct the feedback String.
        feedback = "Generation: " + generationCounter + 
            " Last change: " + lastImprovement + 
            " Best: " + population[0].getFitness() + 
            " Time: " + Math.round((timeSpent / 1000.0) * 10) / 10.0 + "sec" + 
            (delta > 0 ? " (improvement: " + delta +")" : "");
    }

    @Override
    public boolean stoppingConditionMet() {
        return false;
    }

    @Override
    public String getProgressString() {
        return feedback;
    }
    
    @Override
    public int getGenerationCounter() {
        return generationCounter;
    }

}
