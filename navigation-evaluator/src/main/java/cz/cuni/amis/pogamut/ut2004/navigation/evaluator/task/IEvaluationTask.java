package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task;

import cz.cuni.amis.pogamut.ut2004.bot.params.UT2004BotParameters;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.EvaluatingBot;

/**
 *
 * @author Bogo
 */
public interface IEvaluationTask<T extends UT2004BotParameters, X extends EvaluatingBot> {
    
    public String getMapName();
    
    public T getBotParams();
    
    public Class<T> getBotParamsClass();
    
    public Class<X> getBotClass();

    public String getLogPath();

    public String getFileName();
    
}
