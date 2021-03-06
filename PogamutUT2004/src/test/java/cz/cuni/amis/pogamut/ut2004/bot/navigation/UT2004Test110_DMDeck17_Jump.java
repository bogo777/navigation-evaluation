package cz.cuni.amis.pogamut.ut2004.bot.navigation;

import cz.cuni.amis.pogamut.ut2004.bot.UT2004BotTest;
import org.junit.Test;

/**
 *
 * @author Peta Michalik
 */
public class UT2004Test110_DMDeck17_Jump extends UT2004BotTest {

	@Override
	protected String getMapName() {
		return "DM-Deck17";
	}

	@Override
	protected String getGameType() {
		return "BotDeathMatch";
	}

        @Test
	public void test110_jump_1_time() {
		startTest(
			// use NavigationTestBot for the test
			NavigationTestBot.class,
			// timeout: 1 minute
			1,
			// test movement between        start: DM-Deck17.PathNode574, end: DM-Deck17.PathNode580 number of repetitions   both ways
			new NavigationTestBotParameters("DM-Deck17.PathNode574",      "DM-Deck17.PathNode580",    1,                        false)
		);
	}

        @Test
	public void test110_jump_20_time() {


		startTest(
			// use NavigationTestBot for the test
			NavigationTestBot.class,
			// timeout: 4 minutes
			4,
			// test movement between        start: DM-Deck17.PathNode574, end: DM-Deck17.PathNode580 number of repetitions   both ways
			new NavigationTestBotParameters("DM-Deck17.PathNode574",      "DM-Deck17.PathNode580",    20,                        false)
		);
	}

}