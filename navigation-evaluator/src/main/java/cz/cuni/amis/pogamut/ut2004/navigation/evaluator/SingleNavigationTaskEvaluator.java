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

import cz.cuni.amis.pogamut.base.agent.state.level1.IAgentStateDown;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.bot.params.UT2004BotParameters;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.EvaluatingBot;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.ExtendedBotNavigationParameters;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.NavigationEvaluatingBot;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data.RecordType;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.IEvaluationTask;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.INavigationEvaluationTask;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.NavigationEvaluationTask;
import cz.cuni.amis.pogamut.ut2004.server.exception.UCCStartException;
import cz.cuni.amis.pogamut.ut2004.utils.UCCWrapper;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import org.zeroturnaround.zip.ZipUtil;

/**
 *
 * @author Bogo
 */
public class SingleNavigationTaskEvaluator extends SingleTaskEvaluator {

    private String currentLog = null;

    @Override
    public int execute(IEvaluationTask task) {
        //We can run the task without path record in standard Evaluator... 
        if (!task.getClass().isAssignableFrom(NavigationEvaluationTask.class)) {
            return super.execute(task);
        } else {
            NavigationEvaluationTask nTask = (NavigationEvaluationTask) task;
            if (nTask.getRecordType() != RecordType.PATH && nTask.getRecordType() != RecordType.PATH_FAILED) {
                return super.execute(task);
            }
        }

        //We will record single paths and restart ucc regularly...
        int iteration = 0;

        int status = 0;
        UCCWrapper server = null;
        UT2004Bot bot = null;
        int stopTimeout = 1000 * 60 * (360);
        boolean done = false;
        UT2004BotParameters params = task.getBotParams();
        while (!done) {
            try {
                setupLog(task.getLogPath(), iteration);
                ++iteration;
                server = run(task.getMapName());
                System.setProperty("pogamut.ut2004.server.port", Integer.toString(server.getControlPort()));
                UT2004BotRunner<UT2004Bot, UT2004BotParameters> botRunner = new UT2004BotRunner<UT2004Bot, UT2004BotParameters>(task.getBotClass(), "EvaluatingBot", server.getHost(), server.getBotPort());
                //botRunner.setLogLevel(Level.FINE);
                log.fine("Starting evaluation bot.");
                System.out.println("Starting evaluation bot from NavigationTaskEvaluator.");
                bot = botRunner.startAgents(params).get(0); //task.getBotParams()).get(0); //
                bot.awaitState(IAgentStateDown.class, stopTimeout);

            } catch (UCCStartException ex) {
                //Failed to launch UCC!
                status = -1;
                log.throwing(SingleTaskEvaluator.class.getSimpleName(), "execute", ex);
            } catch (PogamutException pex) {
                //Bot execution failed!
                if (bot != null && ((EvaluatingBot) bot.getController()).isCompleted()) {
                    status = 0;
                    log.fine("Evaluation completed");
                    System.out.println("Evaluation completed");
                } else {
                    status = -2;
                    log.throwing(SingleTaskEvaluator.class.getSimpleName(), "execute", pex);
                    System.out.println("Unknown Pogamut exception.");
                }
            } finally {
                if (bot != null && bot.notInState(IAgentStateDown.class)) {
                    bot.stop();
                    bot.kill();
                    //throw new RuntimeException("Bot did not stopped in " + stopTimeout + " ms.");
                    status = 0;
                    System.out.println("Bad termination of bot.");
                }
                if (server != null) {
                    server.stop();
                }
                if (bot != null) {
                    System.out.println("Correct repeat finally..");
                    NavigationEvaluatingBot evalBot = (NavigationEvaluatingBot) bot.getController();
                    if (status == 0) {
                        ExtendedBotNavigationParameters paramsExt = evalBot.getNewExtendedParams();
                        System.out.println("Correct status.");
                        System.out.printf("EVALUATION ITERATION COMPLETED - Processed paths: %d, Remaining paths: %d", paramsExt.getEvaluationResult().getProcessedCount(), paramsExt.getPathContainer().size());
                        if (paramsExt.getEvaluationResult().getProcessedCount() >= paramsExt.getLimitForCompare() || paramsExt.getPathContainer().isEmpty()) {
                            done = true;
                            continue;
                        }
                        params = new ExtendedBotNavigationParameters((INavigationEvaluationTask) task, paramsExt.getPathContainer(), paramsExt.getEvaluationResult());
                        ((ExtendedBotNavigationParameters) params).setIteration(paramsExt.getIteration() + 1);
                    }
                }
                if (status != 0) {
                    done = true;
                }
            }
        }

        System.out.close();

        processResult(task);

        return status;
    }

    protected void setupLog(String logPath, int iteration) {
        if (iteration != 0) {
            System.out.close();
            if (currentLog != null) {
                File currentLogFile = new File(currentLog);
                if (ServerRunner.doCompress()) {
                    ZipUtil.packEntry(currentLogFile, new File(currentLog + ".zip"));
                    currentLogFile.delete();
                }
            }
        }
        try {
            currentLog = String.format("%s-%d.log", logPath.substring(0, logPath.length() - 4), iteration);
            System.setOut(new PrintStream(currentLog));
        } catch (FileNotFoundException ex) {
        }
    }

}
