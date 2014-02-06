package cz.cuni.amis.pogamut.ut2004.navigation.evaluator.task;

import cz.cuni.amis.pogamut.ut2004.bot.params.UT2004BotParameters;
import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.bot.EvaluatingBot;

/**
 *
 * @author Bogo
 */
public abstract class EvaluationTask<T extends UT2004BotParameters, X extends EvaluatingBot> implements IEvaluationTask<T, X> {

    private Class<T> paramsClass;
    private Class<X> botClass;

    public EvaluationTask(Class<T> paramsClass, Class<X> botClass) {
        this.paramsClass = paramsClass;
        this.botClass = botClass;
    }

    public Class<T> getBotParamsClass() {
        return paramsClass;
    }

    public Class<X> getBotClass() {
        return botClass;
    }
}
