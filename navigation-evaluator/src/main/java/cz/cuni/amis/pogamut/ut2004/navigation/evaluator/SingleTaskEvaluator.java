package cz.cuni.amis.pogamut.ut2004.navigation.evaluator;

import cz.cuni.amis.pogamut.base.agent.state.level1.IAgentStateDown;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.bot.params.UT2004BotParameters;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.EvaluatingBot;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.EvaluationTaskFactory;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.IEvaluationTask;
import cz.cuni.amis.pogamut.ut2004.server.exception.UCCStartException;
import cz.cuni.amis.pogamut.ut2004.utils.UCCWrapper;
import cz.cuni.amis.pogamut.ut2004.utils.UCCWrapperConf;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Evaluator of single {@link IEvaluationTask}. Starts its own UCC server and
 * runs single {@link EvaluatingBot} on it.
 *
 * @author Bogo
 */
public class SingleTaskEvaluator {

    private static final Logger log = Logger.getLogger("TaskEvaluator");

    public static void main(String args[]) {

        log.setLevel(Level.ALL);
        log.fine("Running SingleTaskEvaluator");
        IEvaluationTask task = EvaluationTaskFactory.build(args);
        log.fine("Task built from args");
        SingleTaskEvaluator evaluator = new SingleTaskEvaluator();
        int result = evaluator.execute(task);
        System.exit(result);

    }

    private static void setupLog(String logPath) {
        try {
            System.setOut(new PrintStream(logPath));
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }
    }

    public SingleTaskEvaluator() {
    }

    /**
     * Executes given {@link IEvaluationTask}.
     *
     * @param task Task to execute.
     * @return Execution result.
     */
    public int execute(IEvaluationTask task) {
        setupLog(task.getLogPath());
        int status = 0;
        UCCWrapper server = null;
        UT2004Bot bot = null;
        int stopTimeout = 1000 * 60 * (360);
        try {
            server = run(task.getMapName());
            UT2004BotRunner<UT2004Bot, UT2004BotParameters> botRunner = new UT2004BotRunner<UT2004Bot, UT2004BotParameters>(task.getBotClass(), "EvaluatingBot", server.getHost(), server.getBotPort());
            //botRunner.setLogLevel(Level.FINE);
            log.fine("Starting evaluation bot.");
            bot = botRunner.startAgents(task.getBotParams()).get(0);
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
            } else {
                status = -2;
                log.throwing(SingleTaskEvaluator.class.getSimpleName(), "execute", pex);
            }
        } finally {
            if (bot != null && bot.notInState(IAgentStateDown.class)) {
                bot.stop();
                bot.kill();
                //throw new RuntimeException("Bot did not stopped in " + stopTimeout + " ms.");
                status = -3;
            }
            if (server != null) {
                server.stop();
            }
            System.out.close();
            return status;
        }
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
        conf.setGameType("BotDeathMatch");
        conf.setMapName(mapName);

        UCCWrapper server = new UCCWrapper(conf);
        log.fine("UCC server started.");
        return server;
    }
}
