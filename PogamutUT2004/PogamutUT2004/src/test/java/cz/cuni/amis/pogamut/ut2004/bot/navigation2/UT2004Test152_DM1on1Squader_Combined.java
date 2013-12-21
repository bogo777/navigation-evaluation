package cz.cuni.amis.pogamut.ut2004.bot.navigation2;

import cz.cuni.amis.pogamut.ut2004.bot.UT2004BotTest;
import org.junit.Test;

/**
 *
 * @author Peta Michalik
 */
public class UT2004Test152_DM1on1Squader_Combined extends UT2004BotTest {

	@Override
	protected String getMapName() {
		return "DM-1on1-Squader";
	}

	@Override
	protected String getGameType() {
		return "BotDeathMatch";
	}

        @Test
	public void test152_combined_1_time() {
		startTest(
			// use NavigationTestBot for the test
			Navigation2TestBot.class,
			// timeout: 1 minute
			1,
			// test movement between        start: DM-1on1-Squader.PlayerStart7, end: DM-1on1-Squader.PathNode0 number of repetitions   both ways
			new Navigation2TestBotParameters("DM-1on1-Squader.PlayerStart7",      "DM-1on1-Squader.PathNode0",    1,                        true)
		);
	}

        /*
        * TODO: Test fails
        */
        @Test
	public void test152_combined_20_time() {


		startTest(
			// use NavigationTestBot for the test
			Navigation2TestBot.class,
			// timeout: 20 minutes
			20,
			// test movement between        start: DM-1on1-Squader.PlayerStart7, end: DM-1on1-Squader.PathNode0 number of repetitions   both ways
			new Navigation2TestBotParameters("DM-1on1-Squader.PlayerStart7",      "DM-1on1-Squader.PathNode0",    20,                        true)
		);
	}

}
