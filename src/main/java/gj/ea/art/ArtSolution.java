package gj.ea.art;

import java.awt.Image;

public interface ArtSolution {

    /**
     * Return the fitness of the individual.
     * 
     * @return
     */
    public long getFitness();

    /**
     * For screen feedback.
     * 
     * @return
     */
    public Image getScreenSolutionImage();

    /**
     * Potentially the same as getScreenSolution(), but this specifically caters
     * for disk output (i.e. better rendered, etc).
     * 
     * @return
     */
    public Image getDiskSolutionImage();

}
