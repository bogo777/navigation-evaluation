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

import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.IEvaluationTask;
import cz.cuni.amis.utils.exception.PogamutException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Server runner for evaluation. Capable of multiple concurrent evaluations.
 *
 * @author Bogo
 */
public class ServerRunner {

    //Platforms specific paths in one place in code for now.
    public static String recordsPath = System.getProperty("os.name").toLowerCase().contains("linux") ? "/home/bohuslav_machac/UT2004-Dedicated-3369-Linux/Demos" : "C:/Games/UT/Demos";
    public static String executionDir = System.getProperty("os.name").toLowerCase().contains("linux") ? "/home/bohuslav_machac" : "C:/Temp/Pogamut";
    public static String unrealHome = System.getProperty("os.name").toLowerCase().contains("linux") ? "/home/bohuslav_machac/UT2004-Dedicated-3369-Linux" : "C:/Games/UT";

    public ServerRunner() {
        //TODO: Is this duplicating output?
        log.setLevel(Level.ALL);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        log.addHandler(handler);
    }
    
    private List<EvaluatorHandle> evaluations = new LinkedList<EvaluatorHandle>();
    private List<File> tasks;
    private static final Logger log = Logger.getLogger("ServerRunner");

    /**
     * Initializes a list of {@link IEvaluationTask} represented in XML by XSTream serialization. Uses passed directory or current of omitted.
     * 
     * @param args 
     */
    public void initTasks(String[] args) {
        tasks = new ArrayList<File>();

        String evalDir = ".";
        if (args.length == 1) {
            evalDir = args[0];
        }
        File x = new File(evalDir);
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".eval.xml");
            }
        };
        tasks.addAll(Arrays.asList(x.listFiles(filter)));
    }

    /**
     * Main method for ServerRunner. Evaluates task from given directory.
     * 
     * @param args
     * @throws PogamutException 
     */
    public static void main(String args[]) {
        ServerRunner runner = new ServerRunner();

        runner.initTasks(args);

        //Check if there are enough cores available for multiple concurrent evaluations.
        if (hasCapacityForMultiEvaluation()) {
            log.fine("Multi evaluation");
            runner.run();
        } else {
            //Run the evaluation directly
            log.fine("Direct evaluation");
            DirectRunner directRunner = new DirectRunner(runner);
            directRunner.run();
        }

        System.exit(0);
    }

    /**
     * Loop of the runner. Every 5 seconds checks for finished tasks and available resources to start new evaluations.
     */
    private void run() {
        log.log(Level.INFO, "Starting multiple evaluation of {0} tasks", tasks.size());
        boolean done = tasks.isEmpty();
        while (!done) {
            boolean hasFreeTasks = true;
            while (hasCapacity() && hasFreeTasks) {
                File task = getFreeTask();
                if (task == null) {
                    hasFreeTasks = false;
                } else {
                    EvaluatorHandle handle = new EvaluatorHandle();
                    if (handle.createEvaluator(task, log)) {
                        log.fine("Created new evaluation handler");
                        evaluations.add(handle);
                    }
                }
            }

            log.log(Level.INFO, "Tasks in progress: {0}, Unfinished tasks: {1}", new Object[]{evaluations.size(), tasks.size()});
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                //Bla bla
            }
            checkRunningEvaluations();
            done = tasks.isEmpty();
        }


    }

    /**
     * Checks status of current evaluations.
     */
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
                case DESTROYED:
                case COMPLETED:
                    File task = handle.getTask();
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

    /**
     * Check if there are enough cores available for multiple concurrent evaluations.
     * 
     * @return 
     */
    private static boolean hasCapacityForMultiEvaluation() {
        //return true;
        int threshold = 3;
        int available = Runtime.getRuntime().availableProcessors();
        return available >= threshold;
    }

    /**
     * Checks for available resources.
     * 
     * @return 
     */
    private boolean hasCapacity() {
        int used = 0 + evaluations.size() * 2;
        int available = Runtime.getRuntime().availableProcessors();
        return used < available;
    }

    /**
     * Gets new task to evaluate.
     * 
     * @return 
     */
    protected File getFreeTask() {
        for (File task : tasks) {
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

    /**
     * Gets list of tasks.
     * 
     * @return 
     */
    public List<File> getTasks() {
        return tasks;
    }
}
