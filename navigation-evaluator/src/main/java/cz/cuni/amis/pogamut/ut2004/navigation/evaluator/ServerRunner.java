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

import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data.EvaluationRepeatTask;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data.EvaluationTask;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data.RecordType;
import cz.cuni.amis.utils.exception.PogamutException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Server runner for evaluation. Capable of multiple concurrent evaluations.
 *
 * @author Bogo
 */
public class ServerRunner {

    private static boolean hasCapacityForMultiEvaluation() {
        int threshold = 3;
        int available = Runtime.getRuntime().availableProcessors();
        return available >= threshold;
    }

    public ServerRunner() {
    }
    private List<EvaluatorHandle> evaluations = new LinkedList<EvaluatorHandle>();
    private List<EvaluationTask> tasks = initTasks();

    public List<EvaluationTask> initTasks() {
        ArrayList<EvaluationTask> myTasks = new ArrayList<EvaluationTask>();

        //DM-TrainingDay task
        EvaluationTask taskDMTrainingDay = new EvaluationTask("navigation", "fwMap", "DM-TrainingDay", true, 10, "C:/Temp/Pogamut/stats", true, RecordType.PATH);
        myTasks.add(taskDMTrainingDay);

//        //DM-Crash task
//        EvaluationTask taskDMCrash = new EvaluationTask("navigation", "fwMap", "DM-1on1-Crash", true, 10, "C:/Temp/Pogamut/stats/", true, RecordType.FULL);
//        myTasks.add(taskDMCrash);

        //DM-Crash task
//        EvaluationTask taskRepeat = new EvaluationRepeatTask("C:/Temp/Pogamut/stats/navigation_fwMap/DM-1on1-Crash_130114_113337.csv", "navigation", "fwMap", "C:/Temp/Pogamut/stats/", RecordType.FULL);
//        myTasks.add(taskRepeat);

        return myTasks;
    }

    public static void main(String args[]) throws PogamutException {
        ServerRunner runner = new ServerRunner();

        if (hasCapacityForMultiEvaluation()) {
            runner.run();
        } else {
            DirectRunner directRunner = new DirectRunner(runner);
            directRunner.run();
        }

        System.exit(0);
    }

    private void run() {

        boolean done = tasks.isEmpty();
        while (!done) {
            checkRunningEvaluations();
            boolean hasFreeTasks = true;
            while (hasCapacity() && hasFreeTasks) {
                EvaluationTask task = getFreeTask();
                if (task == null) {
                    hasFreeTasks = false;
                } else {
                    EvaluatorHandle handle = new EvaluatorHandle();
                    if (handle.createEvaluator(task)) {
                        evaluations.add(handle);
                    }
                }
            }
            done = tasks.isEmpty();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                //Bla bla
            }
        }


    }

    private void checkRunningEvaluations() {
        List<EvaluatorHandle> finishedHandles = new LinkedList<EvaluatorHandle>();
        for (EvaluatorHandle handle : evaluations) {
            switch (handle.getStatus()) {
                case NEW:
                    break;
                case CREATED:
                    break;
                case RUNNING:
                    break;
                case NOT_RESPONDING:
                    break;
                case FAILED:
                    finishedHandles.add(handle);
                    break;
                case DESTROYED:
                    finishedHandles.add(handle);
                    break;
                case COMPLETED:
                    EvaluationTask task = handle.getTask();
                    tasks.remove(task);
                    finishedHandles.add(handle);
                    break;
                default:
                    throw new AssertionError(handle.getStatus().name());
            }
        }
        for (EvaluatorHandle handle : finishedHandles) {
            evaluations.remove(handle);
        }
    }

    private boolean hasCapacity() {
        int used = 1 + evaluations.size() * 2;
        int available = Runtime.getRuntime().availableProcessors();
        return used < available;
    }

    protected EvaluationTask getFreeTask() {
        for (EvaluationTask task : tasks) {
            boolean isFree = true;
            for (EvaluatorHandle handle : evaluations) {
                if (handle.getTask() == task) {
                    isFree = false;
                    break;
                }
            }
            if (isFree) {
                return task;
            }
        }
        return null;
    }

    public List<EvaluationTask> getTasks() {
        return tasks;
    }
}
