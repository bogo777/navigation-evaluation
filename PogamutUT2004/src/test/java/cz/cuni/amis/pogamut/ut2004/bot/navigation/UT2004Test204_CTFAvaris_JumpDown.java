package cz.cuni.amis.pogamut.ut2004.bot.navigation;

import cz.cuni.amis.pogamut.ut2004.bot.UT2004BotTest;
import org.junit.Test;

/**
 *
 * @author Peta Michalik
 */
public class UT2004Test204_CTFAvaris_JumpDown extends UT2004BotTest {

	@Override
	protected String getMapName() {
		return "CTF-Avaris";
	}

	@Override
	protected String getGameType() {
		return "BotDeathMatch";
	}

        @Test
	public void test204_jumpdown_1_time() {
		startTest(
			// use NavigationTestBot for the test
			NavigationTestBot.class,
			// timeout: 1 minute
			1,
			// test movement between        start: CTF-Avaris.PathNode181, end: CTF-Avaris.PathNode157 number of repetitions   both ways
			new NavigationTestBotParameters("CTF-Avaris.PathNode181",      "CTF-Avaris.PathNode157",    1,                        false)
		);
	}

        @Test
	public void test204_jumpdown_20_time() {


		startTest(
			// use NavigationTestBot for the test
			NavigationTestBot.class,
			// timeout: 3 minutes
			3,
			// test movement between        start: CTF-Avaris.PathNode181, end: CTF-Avaris.PathNode157 number of repetitions   both ways
			new NavigationTestBotParameters("CTF-Avaris.PathNode181",      "CTF-Avaris.PathNode157",    20,                        false)
		);
	}

}