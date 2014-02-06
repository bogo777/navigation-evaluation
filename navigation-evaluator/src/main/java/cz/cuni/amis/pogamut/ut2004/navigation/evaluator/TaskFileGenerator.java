package cz.cuni.amis.pogamut.ut2004.navigation.evaluator;

import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data.RecordType;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.EvaluationTaskFactory;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.IEvaluationTask;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.NavigationEvaluationBatchTaskCreator;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.NavigationEvaluationTask;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Bogo
 */
public class TaskFileGenerator {

    public static String statsPath = true || System.getProperty("os.name").toLowerCase().contains("linux") ? "/home/bohuslav_machac/evaluation/stats" : "C:/Temp/Pogamut/stats";

    public static void main(String args[]) {

        List<IEvaluationTask> myTasks = new LinkedList<IEvaluationTask>();

//        MapPathsBatchTaskCreator mapPathsBatchTask = new MapPathsBatchTaskCreator(1, Arrays.asList("DM-1on1-Albatross", "DM-1on1-Crash"), "navigation", "fwMap", "C:/Temp/Pogamut/mapStats", Arrays.asList(PathType.values()));
//        myTasks.addAll(mapPathsBatchTask.createBatch());

//        MapPathsEvaluationTask taskMapEval = new MapPathsEvaluationTask("DM-1on1-Albatross", "navigation", "fwMap", "C:/Temp/Pogamut/mapStats", PathType.NO_JUMPS);
//        myTasks.add(taskMapEval);
//
        //DM-TrainingDay task
//        NavigationEvaluationTask taskDMTrainingDay = new NavigationEvaluationTask("navigation", "fwMap", "DM-TrainingDay", true, 10, statsPath, RecordType.NONE);
//        myTasks.add(taskDMTrainingDay);
//
////        //DM-Crash task
//        NavigationEvaluationTask taskDMCrash = new NavigationEvaluationTask("navigation", "fwMap", "DM-1on1-Crash", true, 10, statsPath, true, RecordType.NONE);
//        myTasks.add(taskDMCrash);

        List<String> maps = Arrays.asList(
//                "DM-1on1-Albatross",
//                "DM-1on1-Idoma",
//                "DM-1on1-Irondust",
//                "DM-1on1-Mixer",
//                "DM-1on1-Roughinery",
//                "DM-1on1-Desolation",
                "DM-1on1-Crash",
                "DM-TrainingDay");
        myTasks.addAll(NavigationEvaluationBatchTaskCreator.createBatch("navigation", "fwMap", maps, true, 500, statsPath, RecordType.PATH_FAILED));

//        XStream xstream = new XStream();
//        xstream.from

        //DM-Crash task
//        NavigationEvaluationTask taskRepeat = new NavigationEvaluationRepeatTask("C:/Temp/Pogamut/stats/navigation_fwMap/DM-1on1-Crash/160114_112055/data.csv", "navigation", "fwMap", "C:/Temp/Pogamut/stats/", RecordType.FULL);
//        myTasks.add(taskRepeat);

        for (IEvaluationTask task : myTasks) {
            EvaluationTaskFactory.toXml(task, "");
        }
    }
}
