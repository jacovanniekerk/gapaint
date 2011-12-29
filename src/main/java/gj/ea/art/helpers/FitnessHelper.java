package gj.ea.art.helpers;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Submits a solution to calculate the fitness.
 * 
 * @author jaco
 *
 */
public final class FitnessHelper {

    private static final int THREAD_COUNT = 5;

    private static ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT);

    private FitnessHelper() {
    }

    private static long rgb(int e1, int e2) {
        int e1b = e1 & 0xff, e2b = e2 & 0xff;
        int e1g = (e1 >> 8) & 0xff, e2g = (e2 >> 8) & 0xff;
        int e1r = (e1 >> 16) & 0xff, e2r = (e2 >> 16) & 0xff;
        return (long) Math.sqrt(Math.pow(e1b - e2b, 2) + Math.pow(e1g - e2g, 2) + Math.pow(e1r - e2r, 2));
    }

    public static Future<Long> submitNewFitnessTask(final BufferedImage sourceImage, final Future<BufferedImage> target) {
        final int width = sourceImage.getWidth();
        final int height = sourceImage.getHeight();
        Future<Long> result = service.submit(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                long tmp = 0;
                BufferedImage targetImage = target.get();
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int p1 = sourceImage.getRGB(x, y);
                        int p2 = targetImage.getRGB(x, y);
                        long diff = rgb(p1, p2);
                        tmp = tmp + diff;
                    }
                }
                return tmp;
            }
        });
        return result;
    }

    public static long waitForFitness(Future<Long> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
