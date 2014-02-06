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
 *
 * @author Bogo
 */
public class NavigationFactory {

    public static IPathPlanner getPathPlanner(NavigationEvaluatingBot bot) {
        String pathPlannerType = bot.getParams().getPathPlanner();
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

    public static IUT2004Navigation getNavigation(NavigationEvaluatingBot bot, UT2004Bot utBot) {
        IUT2004PathExecutor pathExecutor = new UT2004PathExecutor<ILocated>(
                utBot, bot.getInfo(), bot.getMove(),
                new LoqueNavigator<ILocated>(utBot, bot.getInfo(), bot.getMove(), utBot.getLog()),
                utBot.getLog());

        UT2004GetBackToNavGraph getBackToNavGraph = new UT2004GetBackToNavGraph(utBot, bot.getInfo(), bot.getMove());
        UT2004RunStraight runStraight = new UT2004RunStraight(utBot, bot.getInfo(), bot.getMove());
        return new UT2004Navigation(utBot, pathExecutor, bot.getFwMap(), getBackToNavGraph, runStraight);     
    }
}
