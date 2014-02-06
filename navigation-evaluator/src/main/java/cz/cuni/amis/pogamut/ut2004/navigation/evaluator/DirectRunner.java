package cz.cuni.amis.pogamut.ut2004.navigation.evaluator;

import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data.EvaluationTask;

/**
 *
 * @author Bogo
 */
public class DirectRunner {
    
    private ServerRunner serverRunner;
    
    public DirectRunner(ServerRunner serverRunner) {
        this.serverRunner = serverRunner;
    }
    
    public void run() {
        
        boolean done = serverRunner.getTasks().isEmpty();
        while (!done) {
            EvaluationTask task = serverRunner.getFreeTask();
            if (task == null) {
                break;
            } else {
                SingleTaskEvaluator evaluator = new SingleTaskEvaluator();
                int result = evaluator.execute(task);
                if (result == 0) {
                    serverRunner.getTasks().remove(task);
                }
            }
            done = serverRunner.getTasks().isEmpty();
        }
        
    }
}
