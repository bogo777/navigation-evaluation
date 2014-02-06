package cz.cuni.amis.pogamut.ut2004.navigation.evaluator;

import cz.cuni.amis.pogamut.base.agent.state.level1.IAgentStateDown;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.BotNavigationParameters;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.NavigationEvaluatingBot;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data.EvaluationTask;
import cz.cuni.amis.pogamut.ut2004.server.exception.UCCStartException;
import cz.cuni.amis.pogamut.ut2004.utils.UCCWrapper;
import cz.cuni.amis.pogamut.ut2004.utils.UCCWrapperConf;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import java.util.logging.Level;

/**
 *
 * @author Bogo
 */
public class SingleTaskEvaluator {

    public static void main(String args[]) {
        EvaluationTask task = EvaluationTask.buildFromArgs(args);

        int status = 0;
        UCCWrapper server = null;
        UT2004Bot bot = null;
        int stopTimeout = 500000; //1000 * 60 * (360);
        try {
            server = run(task.getMapName());
            UT2004BotRunner<UT2004Bot, BotNavigationParameters> botRunner = new UT2004BotRunner<UT2004Bot, BotNavigationParameters>(NavigationEvaluatingBot.class, "NavigationEvaluatingBot", server.getHost(), server.getBotPort());
            botRunner.setLogLevel(Level.FINE).setMain(true);
            bot = botRunner.startAgents(task.getBotNavigationParams()).get(0);
            bot.awaitState(IAgentStateDown.class, stopTimeout);

        } catch (UCCStartException ex) {
            //Failed to launch UCC!
            status = -1;
        } catch (PogamutException pex) {
            //Bot execution failed!
            status = -2;
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
            System.exit(status);
        }
    }

    public static UCCWrapper run(String mapName) {
        UCCWrapperConf conf = new UCCWrapperConf();
        conf.setUnrealHome("C:\\Games\\UT");
        conf.setStartOnUnusedPort(true);
        conf.setGameType("BotDeathMatch");
        conf.setMapName(mapName);

        UCCWrapper server = new UCCWrapper(conf);
        return server;
    }
}