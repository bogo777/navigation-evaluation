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
package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot;

import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data.EvaluationResult;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task.INavigationEvaluationTask;

public class ExtendedBotNavigationParameters extends BotNavigationParameters {

    private PathContainer pathContainer;
    private EvaluationResult result;
    private int iteration;

    public ExtendedBotNavigationParameters(BotNavigationParameters params, PathContainer container, EvaluationResult result) {
        this(params.getTask(), container, result);
    }

    public ExtendedBotNavigationParameters(INavigationEvaluationTask task, PathContainer container, EvaluationResult result) {
        super(task);
        this.pathContainer = container;
        this.result = result;
        this.iteration = 1;
    }

    public PathContainer getPathContainer() {
        return pathContainer;
    }

    public void setPathContainer(PathContainer pathContainer) {
        this.pathContainer = pathContainer;
    }

    public EvaluationResult getEvaluationResult() {
        return result;
    }

    public void setEvaluationResult(EvaluationResult result) {
        this.result = result;
    }

    public int getIteration() {
        return iteration;
    }

    public void upIteration() {
        ++iteration;
    }

    public void setIteration(int i) {
        iteration = i;
    }
}
