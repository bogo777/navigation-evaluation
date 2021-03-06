package cz.cuni.amis.pogamut.ut2004.bot.impl;

import org.junit.Test;

import cz.cuni.amis.pogamut.ut2004.bot.impl.test.BotTestContext;
import cz.cuni.amis.pogamut.ut2004.bot.impl.test.ConcurrentBot;
import cz.cuni.amis.pogamut.ut2004.bot.impl.test.PauseResumeBotTest;
import cz.cuni.amis.pogamut.ut2004.factory.guice.remoteagent.UT2004BotFactory;
import cz.cuni.amis.pogamut.ut2004.factory.guice.remoteagent.UT2004BotModule;
import cz.cuni.amis.pogamut.ut2004.test.UT2004Test;
import cz.cuni.amis.utils.test.Repeater;

public class UT2004Test08_MultipleLogicBotsRepeatedAtOnce extends UT2004Test {

    @Test
    public void test() {
    	UT2004BotFactory factory = new UT2004BotFactory(new UT2004BotModule(UT2004BotLogicController.class));
    	BotTestContext testCtx = new BotTestContext(log, factory, ucc.getBotAddress());
    	new ConcurrentBot(5, new Repeater(5, new PauseResumeBotTest(3000))).run(testCtx);
    	
       	System.out.println("---/// TEST OK ///---");
    }
    
}
