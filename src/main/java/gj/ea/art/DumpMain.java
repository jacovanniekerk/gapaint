package gj.ea.art;

import com.google.gson.Gson;
import gj.ea.art.ga.GA;
import gj.ea.art.ga.GASolution;
import gj.ea.art.ga.JsonHelper;

import java.awt.*;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: nicok
 * Date: 12/22/11
 * Time: 7:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class DumpMain {
    
    public static void main(String [] args) throws IOException, ClassNotFoundException {
        
        if (args.length != 2) {
            System.out.println("Provide <in> <out>");
            System.exit(1);
        }
        
        
        String inFilename = args[0];
        String outFilename = args[1];

        ObjectInputStream in = new ObjectInputStream(new FileInputStream(inFilename));
        GA ga = (GA)in.readObject();

        GASolution solution = (GASolution)ga.getBestSolution();
        System.out.println("Dumping solution to " + outFilename + "...");
        FileWriter w = new FileWriter(outFilename);
        w.write(JsonHelper.toJson(solution));
        w.close();
        System.out.println("done");
    }

}
