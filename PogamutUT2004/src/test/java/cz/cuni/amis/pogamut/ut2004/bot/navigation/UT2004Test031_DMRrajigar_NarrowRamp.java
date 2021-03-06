package cz.cuni.amis.pogamut.ut2004.bot.navigation;

import cz.cuni.amis.pogamut.ut2004.bot.UT2004BotTest;
import org.junit.Test;

/**
 *
 * @author Peta Michalik
 */
public class UT2004Test031_DMRrajigar_NarrowRamp extends UT2004BotTest {

	@Override
	protected String getMapName() {
		return "DM-Rrajigar";
	}

	@Override
	protected String getGameType() {
		return "BotDeathMatch";
	}

        @Test
	public void test31_narrow_1_time() {
		startTest(
			// use NavigationTestBot for the test
			NavigationTestBot.class,
			// timeout: 1 minute
			1,
			// test movement between        start: DM-Rrajigar.PathNode94, end: DM-Rrajigar.PathNode75 number of repetitions   both ways
			new NavigationTestBotParameters("DM-Rrajigar.PathNode94",      "DM-Rrajigar.PathNode75",    1,                        true)
		);
	}

        /**
        * TODO: Test fails
        */
        @Test
	public void test31_narrow_20_time() {


		startTest(
			// use NavigationTestBot for the test
			NavigationTestBot.class,
			// timeout: 6 minutes
			6,
			// test movement between        start: DM-Rrajigar.PathNode94, end: DM-Rrajigar.PathNode75 number of repetitions   both ways
			new NavigationTestBotParameters("DM-Rrajigar.PathNode94",      "DM-Rrajigar.PathNode75",    20,                        true)
		);
	}

}