package gj.ea.art.helpers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Submits an image to be rendered from a collection of polygons.
 * 
 * @author jaco
 *
 */
public final class RenderImageHelper {

    private static final int THREAD_COUNT = 5;

    private static ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT);

    private RenderImageHelper() {
    }

    public static Future<BufferedImage> submitNewRenderTask(Polygon[] polygons, Color[] colours, int width, int height, boolean smooth) {
        return submitNewRenderTask(polygons, colours, polygons.length, width, height, smooth);
    }

    public static Future<BufferedImage> submitNewRenderTask(final Polygon[] polygons, final Color[] colours, final int numberOfPolygonsToRender,
                    final int width, final int height, final boolean smooth) {
        
        Future<BufferedImage> result = service.submit(new Callable<BufferedImage>() {
            @Override
            public BufferedImage call() throws Exception {
                BufferedImage attempt = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D g2d = (Graphics2D) attempt.getGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, smooth ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
                g2d.setColor(Color.white);
                g2d.fillRect(0, 0, attempt.getWidth(), attempt.getHeight());
                for (int i = 0; i < numberOfPolygonsToRender; i++) {
                    g2d.setColor(colours[i]);
                    g2d.fillPolygon(polygons[i]);
                }
                return attempt;
            }
        });
        return result;
    }
    
    public static BufferedImage waitForImage(Future<BufferedImage> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
