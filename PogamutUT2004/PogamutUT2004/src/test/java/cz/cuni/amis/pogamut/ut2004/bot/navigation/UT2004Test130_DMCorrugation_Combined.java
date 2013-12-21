package cz.cuni.amis.pogamut.ut2004.bot.navigation;

import cz.cuni.amis.pogamut.ut2004.bot.UT2004BotTest;
import org.junit.Test;

/**
 *
 * @author Peta Michalik
 */
public class UT2004Test130_DMCorrugation_Combined extends UT2004BotTest {

	@Override
	protected String getMapName() {
		return "DM-Corrugation";
	}

	@Override
	protected String getGameType() {
		return "BotDeathMatch";
	}

        @Test
	public void test130_combined_1_time() {
		startTest(
			// use NavigationTestBot for the test
			NavigationTestBot.class,
			// timeout: 1 minute
			1,
			// test movement between        start: DM-Corrugation.PathNode39, end: DM-Corrugation.PathNode10 number of repetitions   both ways
			new NavigationTestBotParameters("DM-Corrugation.PathNode39",      "DM-Corrugation.PathNode10",    1,                        true)
		);
	}

        /*
        * TODO: Test fails
        */
        @Test
	public void test130_combined_20_time() {


		startTest(
			// use NavigationTestBot for the test
			NavigationTestBot.class,
			// timeout: 10 minutes
			10,
			// test movement between        start: DM-Corrugation.PathNode39, end: DM-Corrugation.PathNode10 number of repetitions   both ways
			new NavigationTestBotParameters("DM-Corrugation.PathNode39",      "DM-Corrugation.PathNode10",    20,                        true)
		);
	}

}