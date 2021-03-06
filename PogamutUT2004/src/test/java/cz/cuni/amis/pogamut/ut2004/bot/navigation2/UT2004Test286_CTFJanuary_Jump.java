package cz.cuni.amis.pogamut.ut2004.bot.navigation2;

import cz.cuni.amis.pogamut.ut2004.bot.UT2004BotTest;
import org.junit.Test;

/**
 *
 * @author Peta Michalik
 */
public class UT2004Test286_CTFJanuary_Jump extends UT2004BotTest {

	@Override
	protected String getMapName() {
		return "CTF-January";
	}

	@Override
	protected String getGameType() {
		return "BotDeathMatch";
	}

        @Test
	public void test286_jump_1_time() {
		startTest(
			// use NavigationTestBot for the test
			Navigation2TestBot.class,
			// timeout: 1 minute
			1,
			// test movement between        start: CTF-January.PlayerStart15, end: CTF-January.PathNode19 number of repetitions   both ways
			new Navigation2TestBotParameters("CTF-January.PlayerStart15",      "CTF-January.PathNode19",    1,                        true)
		);
	}

        /*
         * TODO: Test fails
         */
        @Test
	public void test286_jump_20_time() {


		startTest(
			// use NavigationTestBot for the test
			Navigation2TestBot.class,
			// timeout: 4 minutes
			4,
			// test movement between        start: CTF-January.PlayerStart15, end: CTF-January.PathNode19 number of repetitions   both ways
			new Navigation2TestBotParameters("CTF-January.PlayerStart15",      "CTF-January.PathNode19",    20,                        true)
		);
	}

}
