package cz.cuni.amis.pogamut.ut2004.bot.navigation;

import cz.cuni.amis.pogamut.ut2004.bot.UT2004BotTest;
import org.junit.Test;

/**
 *
 * @author Peta Michalik
 */
public class UT2004Test072_DMIronDeity_JumpDown extends UT2004BotTest {

	@Override
	protected String getMapName() {
		return "DM-IronDeity";
	}

	@Override
	protected String getGameType() {
		return "BotDeathMatch";
	}

        @Test
	public void test72_jumpdown_1_time() {
		startTest(
			// use NavigationTestBot for the test
			NavigationTestBot.class,
			// timeout: 1 minute
			1,
			// test movement between        start: DM-IronDeity.PathNode0, end: DM-IronDeity.JumpSpot2 number of repetitions   both ways
			new NavigationTestBotParameters("DM-IronDeity.PathNode0",      "DM-IronDeity.JumpSpot2",    1,                        false)
		);
	}

        @Test
	public void test72_jumpdown_20_time() {


		startTest(
			// use NavigationTestBot for the test
			NavigationTestBot.class,
			// timeout: 10 minutes
			10,
			// test movement between        start: DM-IronDeity.PathNode0, end: DM-IronDeity.JumpSpot2 number of repetitions   both ways
			new NavigationTestBotParameters("DM-IronDeity.PathNode0",      "DM-IronDeity.JumpSpot2",    20,                        false)
		);
	}

}