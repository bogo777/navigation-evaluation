/*
 * Copyright (C) 2013 AMIS research group, Faculty of Mathematics and Physics, Charles University in Prague, Czech Republic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cuni.amis.pogamut.ut2004.navigation.evaluator;

import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.EvaluationTaskFactory;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.IEvaluationTask;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class for generating task files from code.
 *
 * @author Bogo
 */
public class TaskFileGenerator {

    public static String statsPath = true || System.getProperty("os.name").toLowerCase().contains("linux") ? "/home/bohuslav_machac/evaluation/stats" : "C:/Temp/Pogamut/stats";

    public static void main(String args[]) {

        List<IEvaluationTask> myTasks = new LinkedList<IEvaluationTask>();

        List<String> maps = Arrays.asList(
                "DM-1on1-Albatross",
                "DM-1on1-Idoma",
                "DM-1on1-Irondust",
                "DM-1on1-Mixer",
                "DM-1on1-Roughinery",
                "DM-1on1-Desolation",
                "DM-1on1-Crash",
                "DM-TrainingDay");
        
        //MapPathsBatchTaskCreator mapPathsBatchTask = new MapPathsBatchTaskCreator(3, MapPathsBatchTaskCreator.getAllMaps(), "navigation", "fwMap", statsPath + "/maps", Arrays.asList(PathType.values()), false);
        //myTasks.addAll(mapPathsBatchTask.createBatch());

//        MapPathsEvaluationTask taskMapEval = new MapPathsEvaluationTask("DM-1on1-Albatross", "navigation", "fwMap", "C:/Temp/Pogamut/mapStats", PathType.NO_JUMPS);
//        myTasks.add(taskMapEval);
//
        //DM-TrainingDay task
//        NavigationEvaluationTask taskDMTrainingDay = new NavigationEvaluationTask("navigation", "fwMap", "DM-TrainingDay", true, -1, statsPath, RecordType.FULL);
//        myTasks.add(taskDMTrainingDay);
//
////        //DM-Crash task
//        NavigationEvaluationTask taskDMCrash = new NavigationEvaluationTask("navigation", "fwMap", "DM-1on1-Crash", true, 10, statsPath, true, RecordType.NONE);
//        myTasks.add(taskDMCrash);

//        List<String> maps = Arrays.asList(
//                "DM-1on1-Albatross",
//                "DM-1on1-Idoma",
//                "DM-1on1-Irondust",
//                "DM-1on1-Mixer",
//                "DM-1on1-Roughinery",
//                "DM-1on1-Desolation",
//                "DM-1on1-Crash",
//                "DM-TrainingDay");
//        myTasks.addAll(NavigationEvaluationBatchTaskCreator.createBatch("navigation", "fwMap", maps, true, -1, statsPath, RecordType.NONE));

//        XStream xstream = new XStream();
//        xstream.from

        //DM-Crash task
//        NavigationEvaluationTask taskRepeat = new NavigationEvaluationRepeatTask("C:/Temp/Pogamut/stats/navigation_fwMap/DM-1on1-Crash/160114_112055/data.csv", "navigation", "fwMap", "C:/Temp/Pogamut/stats/", RecordType.FULL);
//        myTasks.add(taskRepeat);

        for (IEvaluationTask task : myTasks) {
            EvaluationTaskFactory.toXml(task, "C:/Temp/Pogamut/060214_maps_all/");
        }
    }
}
