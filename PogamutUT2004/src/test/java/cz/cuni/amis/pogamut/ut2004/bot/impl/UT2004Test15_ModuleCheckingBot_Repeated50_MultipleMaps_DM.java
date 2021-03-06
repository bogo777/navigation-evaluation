package cz.cuni.amis.pogamut.ut2004.bot.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cz.cuni.amis.pogamut.base.utils.Pogamut;
import cz.cuni.amis.pogamut.ut2004.bot.impl.test.BotContext;
import cz.cuni.amis.pogamut.ut2004.bot.impl.test.BotTestContext;
import cz.cuni.amis.pogamut.ut2004.bot.impl.test.ModuleCheckingBot;
import cz.cuni.amis.pogamut.ut2004.bot.impl.test.SimpleBotTest;
import cz.cuni.amis.pogamut.ut2004.factory.guice.remoteagent.UT2004BotFactory;
import cz.cuni.amis.pogamut.ut2004.factory.guice.remoteagent.UT2004BotModule;
import cz.cuni.amis.pogamut.ut2004.server.exception.UCCStartException;
import cz.cuni.amis.pogamut.ut2004.test.UT2004Test;
import cz.cuni.amis.pogamut.ut2004.utils.UCCWrapper;
import cz.cuni.amis.pogamut.ut2004.utils.UCCWrapperConf;

public class UT2004Test15_ModuleCheckingBot_Repeated50_MultipleMaps_DM extends UT2004Test {

	/**
     * Initialize UCC server.
     * @throws UCCStartException
     */
    @Before
    public void beforeTest() throws UCCStartException {
    	if (!useInternalUcc) {
    		throw new RuntimeException("This test must be run with useInternalUcc == true!!!");
    	}
    }

    /**
     * Kills the UCC server and closes PogamutPlatform.
     */
    @After
    public void afterTest() {
    	ucc = null;
    	Pogamut.getPlatform().close();
    }  
	
    @Test
    public void testDM() {
    	
    	UT2004BotFactory factory = new UT2004BotFactory(new UT2004BotModule(ModuleCheckingBot.class));
    	
    	int j = 0;
    	for (String map : DM_MAPS) {
    		++j;
    		System.out.println("[INFO] {DM} (" + map + ") MAP " + j + " / " + DM_MAPS.length);
    		try {
    			ucc = new UCCWrapper(new UCCWrapperConf().setMapName(map).setGameType("BotDeathMatch").setStartOnUnusedPort(true));
	        	BotTestContext testCtx = new BotTestContext(log, factory, ucc.getBotAddress());
	    		for (int i = 0; i < 50; ++i) {
	    			System.out.println("[INFO] {DM} (" + map + ") MAP " + j + " / " + DM_MAPS.length + ", TEST " + (i+1) + " / 50");
	    			BotContext botCtx = testCtx.newBotContext();
	    			new SimpleBotTest().run(botCtx);
	    			System.out.println("[FIN]  {DM} (" + map + ") MAP " + j + " / " + DM_MAPS.length + ", TEST " + (i+1) + " / 50");
	    		}
    		} finally {
    			if (ucc != null) {
    				ucc.stop();
    			}
    		}
    	}
    	
       	System.out.println("---/// TEST OK ///---");
    }

}
