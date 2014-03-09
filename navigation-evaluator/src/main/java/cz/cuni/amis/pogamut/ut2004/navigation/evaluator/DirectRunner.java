/*
 * Copyright (C) 2013 AMIS research group, Faculty of Mathematics and Physics, Charles University in Prague, Czech Republic
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
package cz.cuni.amis.pogamut.ut2004.navigation.evaluator;

import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.EvaluationTaskFactory;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.IEvaluationTask;
import java.io.File;

/**
 * Runs evaluation directly in its thread. Starts only one UCC server and bot at a time.
 *
 * @author Bogo
 */
public class DirectRunner {

    private ServerRunner serverRunner;

    public DirectRunner(ServerRunner serverRunner) {
        this.serverRunner = serverRunner;
    }

    public void run(boolean isResume) {

        boolean done = serverRunner.getTasks().isEmpty();
        while (!done) {
            File taskFile = serverRunner.getFreeTask();
            IEvaluationTask task = EvaluationTaskFactory.build(taskFile);
            if (task == null) {
                break;
            } else {
                SingleNavigationTaskEvaluator evaluator = new SingleNavigationTaskEvaluator();
                int result = evaluator.execute(task, isResume);
                if (result == 0) {
                    serverRunner.getTasks().remove(taskFile);
                }
            }
            done = serverRunner.getTasks().isEmpty();
        }

    }
}
