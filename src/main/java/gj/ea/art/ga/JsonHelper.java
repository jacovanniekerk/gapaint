package gj.ea.art.ga;

import com.google.gson.Gson;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: nicok
 * Date: 12/31/11
 * Time: 7:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class JsonHelper {

    public static String toJson(GASolution s) {
        return (new Gson()).toJson(toJsonObject(s));
    }

    static JsonOutput toJsonObject(GASolution s) {
        // map
        JsonPolygon[] mapped = new JsonPolygon[s.polygonCount];
        for (int i = 0; i < s.polygonCount; i++) {
            mapped[i] = new JsonPolygon();
            mapped[i].polygon = new JsonPoint[s.polys[i].npoints];
            for (int k = 0; k < s.polys[i].npoints; k++) {
                mapped[i].polygon[k] = new JsonPoint();
                mapped[i].polygon[k].x = s.polys[i].xpoints[k];
                mapped[i].polygon[k].y = -1 * s.polys[i].ypoints[k]; // jsdoom engine is cartesian - need to flip the y
            }
            Color col = s.cols[i];
            JsonColor c = new JsonColor();
            c.r = col.getRed();
            c.g = col.getGreen();
            c.b = col.getBlue();
            c.a = col.getAlpha();

            mapped[i].color = c;
        }

        JsonOutput h = new JsonOutput();
        h.polys = mapped;
        h.backgroundColor = "#ffffff";
        return h;
    }

    static class JsonColor {
        int r;
        int g;
        int b;
        int a;
    }

    static class JsonPoint {
        float x;
        float y;
    }
    static class JsonPolygon {
        JsonPoint[] polygon;
        JsonColor color;
    }

    static class JsonOutput {
        JsonPolygon[] polys;
        String backgroundColor;
    }

}
