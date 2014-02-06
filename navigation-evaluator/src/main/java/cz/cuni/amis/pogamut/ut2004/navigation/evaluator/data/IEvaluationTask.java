package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data;

/**
 *
 * @author Bogo
 */
public interface IEvaluationTask {

    public void setLimit(int limit);

    public String getPathPlanner();

    public int getLimit();

    public void setOnlyRelevantPaths(boolean onlyRelevantPaths);

    public boolean isOnlyRelevantPaths();

    public void setResultPath(String resultPath);

    public void setPathPlanner(String pathPlanner);

    public String getNavigation();

    public void setNavigation(String navigation);

    public boolean isResultUnique();

    public void setResultUnique(boolean resultUnique);
    
    public String getResultPath();
    
    public String getMapName();
    
    public RecordType getRecordType();
    
}
