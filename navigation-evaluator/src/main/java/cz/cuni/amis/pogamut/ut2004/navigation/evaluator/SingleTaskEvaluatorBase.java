/*
 * Copyright (C) 2014 AMIS research group, Faculty of Mathematics and Physics, Charles University in Prague, Czech Republic
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
import cz.cuni.amis.pogamut.ut2004.utils.UCCWrapper;
import cz.cuni.amis.pogamut.ut2004.utils.UCCWrapperConf;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bogo
 */
public abstract class SingleTaskEvaluatorBase {
    protected static final Logger log = Logger.getLogger("TaskEvaluator");

    /**
     * Main method. Accepts path to task file in args.
     *
     * @param args
     */
    public static void main(String[] args) {
        log.setLevel(Level.ALL);
        log.fine("Running SingleTaskEvaluator");
        IEvaluationTask task = EvaluationTaskFactory.build(args);
        log.fine("Task built from args");
        SingleNavigationTaskEvaluator evaluator = new SingleNavigationTaskEvaluator();
        int result = evaluator.execute(task);
        System.exit(result);
    }

    /**
     * Starts UCC server.
     *
     * @param mapName Map which start on server.
     * @return Server wrapper.
     */
    public static UCCWrapper run(String mapName) {
        log.fine("UCC server starting...");
        UCCWrapperConf conf = new UCCWrapperConf();
        conf.setUnrealHome(ServerRunner.unrealHome);
        conf.setStartOnUnusedPort(true);
        String gameType = MapInfo.getGameType(mapName);
        conf.setGameType(gameType);
        conf.setMapName(mapName);
        UCCWrapper server = new UCCWrapper(conf);
        log.fine("UCC server started.");
        return server;
    }

    /**
     * Redirects out to save log in file.
     *
     * @param logPath
     */
    protected static void setupLog(String logPath) {
        try {
            System.setOut(new PrintStream(logPath));
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }
    }

    /**
     * Executes given {@link IEvaluationTask}.
     *
     * @param task Task to execute.
     * @return Execution result.
     */
    public abstract int execute(IEvaluationTask task);
    
}
