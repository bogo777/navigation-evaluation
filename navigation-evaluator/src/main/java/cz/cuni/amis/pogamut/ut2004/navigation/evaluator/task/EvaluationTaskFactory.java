package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task;

import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Bogo
 */
public class EvaluationTaskFactory {

    public static IEvaluationTask build(String[] args) {
        if (args.length == 1) {

            File file = new File(args[0]);
            return build(file);

        } else {


            return NavigationEvaluationTask.buildFromArgs(args);
        }
    }

    public static void toArgs(IEvaluationTask task, ArrayList<String> command) {
        ((NavigationEvaluationTask) task).toArgs(command);
    }

    public static void toXml(IEvaluationTask task, String directory) {
        FileWriter writer = null;
        try {
            String fileName = task.getFileName() + ".eval.xml";
            XStream xstream = new XStream();
            writer = new FileWriter(fileName);
            xstream.toXML(task, writer);
        } catch (IOException ex) {
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    public static IEvaluationTask build(File freeTask) {
        XStream xstream = new XStream();
        FileReader reader;
        try {
            reader = new FileReader(freeTask);
        } catch (FileNotFoundException ex) {
            return null;
        }
        return (IEvaluationTask) xstream.fromXML(reader);
    }
}
