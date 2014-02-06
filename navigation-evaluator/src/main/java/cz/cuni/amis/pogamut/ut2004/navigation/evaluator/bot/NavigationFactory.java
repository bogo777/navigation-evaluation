package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot;

import cz.cuni.amis.pogamut.base.agent.navigation.IPathPlanner;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004PathExecutor;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004GetBackToNavGraph;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004PathExecutor;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004RunStraight;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.loquenavigator.LoqueNavigator;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMesh;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory class for creating custom navigations from given parameters.
 * Navigation must be created at bot initialization and this allows for
 * navigation to be specified before execution without modifying bot's source
 * code.
 *
 * @author Bogo
 */
public class NavigationFactory {

    /**
     * Get {@link IPathPlanner} specified in params for given bot .
     *
     * @param bot
     * @return
     *
     */
    public static IPathPlanner getPathPlanner(EvaluatingBot bot, String pathPlannerType) {
        if (pathPlannerType != null) {
            if (pathPlannerType.equals("fwMap")) {
                return bot.getFwMap();
            } else if (pathPlannerType.equals("navMesh")) {
                try {
                    return new NavMesh();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(NavigationFactory.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(NavigationFactory.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return bot.getFwMap();
    }

    /**
     * Get {@link IUT2004Navigation} specified in params for given bot .
     *
     * @param bot
     * @return
     *
     */
    public static IUT2004Navigation getNavigation(EvaluatingBot bot, UT2004Bot utBot, String navigationType) {
        IUT2004PathExecutor pathExecutor = new UT2004PathExecutor<ILocated>(
                utBot, bot.getInfo(), bot.getMove(),
                new LoqueNavigator<ILocated>(utBot, bot.getInfo(), bot.getMove(), utBot.getLog()),
                utBot.getLog());

        UT2004GetBackToNavGraph getBackToNavGraph = new UT2004GetBackToNavGraph(utBot, bot.getInfo(), bot.getMove());
        UT2004RunStraight runStraight = new UT2004RunStraight(utBot, bot.getInfo(), bot.getMove());
        return new UT2004Navigation(utBot, pathExecutor, bot.getFwMap(), getBackToNavGraph, runStraight);
    }

    static void initializePathContainer(PathContainer pathContainer, NavigationEvaluatingBot bot) {
        BotNavigationParameters params = bot.getParams();
        if (params.isRepeatTask()) {
            pathContainer.buildFromFile(params.getRepeatFile());
        } else if (params.isOnlyRelevantPaths()) {
            pathContainer.buildRelevant(bot.getParams().getLimit());
        } else {
            pathContainer.build(bot.getParams().getLimit());
        }



    }
}
