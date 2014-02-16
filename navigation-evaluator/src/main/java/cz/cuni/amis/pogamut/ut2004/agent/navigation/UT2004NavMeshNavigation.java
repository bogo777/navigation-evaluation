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
package cz.cuni.amis.pogamut.ut2004.agent.navigation;

import cz.cuni.amis.pogamut.base.agent.navigation.IPathFuture;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Stop;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.utils.flag.Flag;
import cz.cuni.amis.utils.flag.FlagListener;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Bogo
 */
public class UT2004NavMeshNavigation implements IUT2004Navigation {
    
    protected LogCategory log;
    
    /**
     * Whether navigation is running.
     */
    protected boolean navigating = false;
    
    
    /**
     * Last location target.
     */
    protected ILocated lastTarget = null;
    /**
     * Last location target.
     */
    protected Player lastTargetPlayer = null;
    /**
     * Current location target.
     */
    protected ILocated currentTarget = null;
    /**
     * Current target is player (if not null)
     */
    protected Player currentTargetPlayer = null;
    
    /**
     * UT2004PathExecutor that is used for the navigation.
     */
    protected IUT2004PathExecutor<ILocated> pathExecutor;
    
    /**
     * UT2004Bot reference.
     */
    protected UT2004Bot bot;
    
    /**
     * UT2004RunStraight is used to run directly to player at some moment.
     */
    protected IUT2004RunStraight runStraight;
    
    /**
     * State of UT2004Navigation
     */
    protected Flag<NavigationState> state = new Flag<NavigationState>(NavigationState.STOPPED);

    public void addStrongNavigationListener(FlagListener<NavigationState> listener) {
        state.addStrongListener(listener);
    }

    public void removeStrongNavigationListener(FlagListener<NavigationState> listener) {
        state.removeListener(listener);
    }

    public IUT2004PathExecutor<ILocated> getPathExecutor() {
        return pathExecutor;
    }

    /**
     * TODO: Evaluate if we need/want this. 
     * Update: Possibly yes, but accustomed to navMesh, e.g. test if we are in any polygon OR near off mesh navpoint...
     * @return 
     * 
     */
    public IUT2004GetBackToNavGraph getBackToNavGraph() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public IUT2004RunStraight getRunStraight() {
        return runStraight;
    }

    public boolean isNavigating() {
        return navigating;
    }

    public boolean isNavigatingToNavPoint() {
        return navigating && getCurrentTarget() instanceof NavPoint;
    }

    public boolean isNavigatingToItem() {
        return navigating && getCurrentTarget() instanceof Item;
    }

    public boolean isNavigatingToPlayer() {
        return navigating && getCurrentTarget() instanceof Player;
    }

    public boolean isTryingToGetBackToNav() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isPathExecuting() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isRunningStraight() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ILocated getFocus() {
        return pathExecutor.getFocus();
    }

    public void setFocus(ILocated located) {
        pathExecutor.setFocus(located);
        //getBackToNavGraph.setFocus(located);
        runStraight.setFocus(located);
    }

    public void stopNavigation() {
        //TODO: reset()
        bot.getAct().act(new Stop());
    }

    public void navigate(ILocated target) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void navigate(Player player) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void navigate(IPathFuture<ILocated> pathHandle) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setContinueTo(ILocated target) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ILocated getContinueTo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public NavPoint getNearestNavPoint(ILocated location) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public List<ILocated> getCurrentPathCopy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public List<ILocated> getCurrentPathDirect() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

        @Override
    public ILocated getCurrentTarget() {
        return currentTarget;
    }


    public Player getCurrentTargetPlayer() {
        return currentTargetPlayer;
    }

    public Item getCurrentTargetItem() {
        if (currentTarget instanceof Item) {
            return (Item) currentTarget;
        }
        return null;
    }

    public NavPoint getCurrentTargetNavPoint() {
        if (currentTarget instanceof NavPoint) {
            return (NavPoint) currentTarget;
        }
        return null;
    }

    public ILocated getLastTarget() {
        return lastTarget;
    }

    public Player getLastTargetPlayer() {
        return lastTargetPlayer;
    }

    public Item getLastTargetItem() {
        if(lastTarget instanceof Item) {
            return (Item)lastTarget;
        }
        return null;
    }

    public Flag<NavigationState> getState() {
        return state;
    }

    public double getRemainingDistance() {
        if (!isNavigating()) {
            return 0;
        }
        //TODO: Complete
        return 5;
    }

    public Logger getLog() {
        return log;
    }
    
}
