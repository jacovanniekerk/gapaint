package gj.ea.art;

import java.util.Properties;

public interface EvolutionaryAlgorithm {

    /**
     * Called once before EA starts, passing the image to evolve.
     */
    public void initialise(String sourceImageFile, Properties properties);
    
    /**
     * Returns true if the algorithm should stop. This can potentially just
     * always returns false to create an infinite loop scenario. Graceful
     * termination will still be possible.
     * 
     * @return
     */
    public boolean stoppingConditionMet();

    /**
     * Complete a single iteration. This must be implemented in a thread safe
     * fashion!
     */
    public void iterate();

    /**
     * Get the best solution in the current population.
     * 
     * @return
     */
    public ArtSolution getBestSolution();

    /**
     * Returns a String describing the progress of the algorithm.
     * 
     * @return
     */
    public String getProgressString();
    
    public int getGenerationCounter();
    
    
}
