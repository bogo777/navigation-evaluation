package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot;

import cz.cuni.amis.pogamut.base.agent.navigation.IPathFuture;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.astar.UT2004AStar;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.floydwarshall.FloydWarshallMap;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004DistanceStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004PositionStuckDetector;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.UT2004TimeStuckDetector;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPointNeighbourLink;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data.MapPathsResult;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.MapPathsEvaluationTask.PathType;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author Bogo
 */
public class MapPathsBot extends EvaluatingBot {

    public MapPathsBotParameters getParams() {
        return (MapPathsBotParameters) bot.getParams();
    }

    @Override
    protected void initializePathFinding(UT2004Bot bot) {
        fwMap = new FloydWarshallMap(bot);
        pathPlanner = NavigationFactory.getPathPlanner(this, getParams().getPathPlanner());
        navigation = NavigationFactory.getNavigation(this, bot, getParams().getNavigation());

        aStar = new UT2004AStar(bot);
        pathExecutor = navigation.getPathExecutor();

        // add stuck detectors that watch over the path-following, if it (heuristicly) finds out that the bot has stuck somewhere,
        // it reports an appropriate path event and the path executor will stop following the path which in turn allows 
        // us to issue another follow-path command in the right time
        pathExecutor.addStuckDetector(new UT2004TimeStuckDetector(bot, 3000, 100000)); // if the bot does not move for 3 seconds, considered that it is stuck
        pathExecutor.addStuckDetector(new UT2004PositionStuckDetector(bot));           // watch over the position history of the bot, if the bot does not move sufficiently enough, consider that it is stuck
        pathExecutor.addStuckDetector(new UT2004DistanceStuckDetector(bot));           // watch over distances to target

        getBackToNavGraph = navigation.getBackToNavGraph();
        runStraight = navigation.getRunStraight();
    }

    @Override
    public Initialize getInitializeCommand() {
        return new Initialize().setName("MapPathsBot");
    }

    /**
     * This method is called only once right before actual logic() method is
     * called for the first time.
     */
    @Override
    public void beforeFirstLogic() {

        PathType pathType = getParams().getPathType();

        MapPathsResult result = new MapPathsResult(getParams().getTask(), log);


        //Preprocessing navigation graph - removing disabled links
        for (Entry<UnrealId, NavPoint> entry : navPoints.getNavPoints().entrySet()) {
            NavPoint from = entry.getValue();
            boolean removeAll = from.isLiftCenter() && removeLifts(pathType);

            HashMap<UnrealId, NavPointNeighbourLink> outgoingEdges = new HashMap<UnrealId, NavPointNeighbourLink>(from.getOutgoingEdges());
            for (Entry<UnrealId, NavPointNeighbourLink> edgeEntry : outgoingEdges.entrySet()) {
                if (removeAll) {
                    navBuilder.removeEdgesBetween(entry.getKey().getStringId(), edgeEntry.getKey().getStringId());
                } else {
                    NavPointNeighbourLink edge = edgeEntry.getValue();
                    int edgeFlags = edge.getFlags();
                    boolean remove = !evaluateFlags(edgeFlags, pathType);
                    if (remove) {
                        navBuilder.removeEdge(entry.getKey().getStringId(), edgeEntry.getKey().getStringId());
                    }
                }
            }
        }

        //Recomputing fwMap
        fwMap.refreshPathMatrix();

        //Path creation evaluation
        for (NavPoint from : navPoints.getNavPoints().values()) {
            for (NavPoint to : navPoints.getNavPoints().values()) {
                if (from.equals(to)) {
                    continue;
                }

                IPathFuture<NavPoint> path = fwMap.computePath(from, to);
                if (path == null || path.get() == null) {
                    //Failed to build path
                    result.addFailed();
                } else {
                    result.addSuccessful();
                }
            }
        }

        //Wrap up and end
        result.export();

        isCompleted = true;

        bot.stop();

    }

    private boolean evaluateFlags(int edgeFlags, PathType pathType) {
        //from http://wiki.beyondunreal.com/Legacy:ReachSpec
        int jumpFlag = 8;
        boolean isJump = (edgeFlags & jumpFlag) == jumpFlag;
        switch (pathType) {
            case ALL:
            case NO_LIFTS:
                return true;
            case NO_JUMPS:
            case NO_JUMP_NO_LIFTS:
                return !isJump;
            default:
                throw new AssertionError(pathType.name());
        }
    }

    private boolean removeLifts(PathType pathType) {
        return pathType == PathType.NO_LIFTS || pathType == PathType.NO_JUMP_NO_LIFTS;
    }
}
