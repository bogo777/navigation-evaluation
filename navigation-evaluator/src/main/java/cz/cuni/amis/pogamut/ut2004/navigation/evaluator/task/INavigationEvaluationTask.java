package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task;

import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.BotNavigationParameters;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.NavigationEvaluatingBot;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data.RecordType;

/**
 *
 * @author Bogo
 */
public interface INavigationEvaluationTask extends IEvaluationTask<BotNavigationParameters, NavigationEvaluatingBot> {

    public void setLimit(int limit);

    public String getPathPlanner();

    public int getLimit();

    public void setOnlyRelevantPaths(boolean onlyRelevantPaths);

    public boolean isOnlyRelevantPaths();

    public void setResultPath(String resultPath);

    public void setPathPlanner(String pathPlanner);

    public String getNavigation();

    public void setNavigation(String navigation);
    
    public String getResultPath();
    
    public RecordType getRecordType();
    
}
