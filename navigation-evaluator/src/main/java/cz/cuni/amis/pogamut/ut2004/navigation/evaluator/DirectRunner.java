package cz.cuni.amis.pogamut.ut2004.navigation.evaluator;

import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.EvaluationTaskFactory;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.IEvaluationTask;
import java.io.File;

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
            File taskFile = serverRunner.getFreeTask();
            IEvaluationTask task = EvaluationTaskFactory.build(taskFile);
            if (task == null) {
                break;
            } else {
                SingleTaskEvaluator evaluator = new SingleTaskEvaluator();
                int result = evaluator.execute(task);
                if (result == 0) {
                    serverRunner.getTasks().remove(taskFile);
                }
            }
            done = serverRunner.getTasks().isEmpty();
        }

    }
}
