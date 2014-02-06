package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot;

import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;

/**
 *
 * @author Bogo
 */
public abstract class EvaluatingBot extends UT2004BotModuleController {
    
    protected boolean isCompleted;
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
}
