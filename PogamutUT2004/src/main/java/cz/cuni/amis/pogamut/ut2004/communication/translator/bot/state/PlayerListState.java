package cz.cuni.amis.pogamut.ut2004.communication.translator.bot.state;

import cz.cuni.amis.fsm.FSMState;
import cz.cuni.amis.fsm.FSMTransition;
import cz.cuni.amis.fsm.IFSMState;
import cz.cuni.amis.pogamut.base.communication.messages.InfoMessage;
import cz.cuni.amis.pogamut.base.communication.translator.event.IWorldChangeEvent;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerListEnd;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerListStart;
import cz.cuni.amis.pogamut.ut2004.communication.translator.TranslatorContext;
import cz.cuni.amis.pogamut.ut2004.communication.translator.bot.support.BotListState;
import cz.cuni.amis.pogamut.ut2004.communication.translator.shared.events.InitCommandRequest;
import cz.cuni.amis.pogamut.ut2004.communication.translator.shared.events.PlayerListObtained;

/**
 * Takes care of the player list. It stores them inside a List object and when END message comes it sends
 * them to the world view via PlayerListObtained event.
 * <p><p>
 * Last state of the handshake - we switch from this state to the ConfigureMessageExpectedState. 
 * 
 * @author Jimmy
 */
@FSMState(map={@FSMTransition(
					state=HandshakeControllerState.class, 
					symbol={PlayerListEnd.class},
					transition={})}
)
public class PlayerListState extends BotListState<Player, TranslatorContext> {

	public PlayerListState() {
		super(PlayerListStart.class, Player.class, PlayerListEnd.class);
	}
	
	@Override
	public void stateLeaving(TranslatorContext context,
			IFSMState<InfoMessage, TranslatorContext> toState, InfoMessage symbol) {
		super.stateLeaving(context, toState, symbol);
		long simTime = 
			(symbol instanceof IWorldChangeEvent ? ((IWorldChangeEvent)symbol).getSimTime() : 0);
		context.getEventQueue().pushEvent(new PlayerListObtained(getList(), simTime));
		newList();
	}
	
	@Override
	public void stateSymbol(TranslatorContext context, InfoMessage symbol) {
		super.stateSymbol(context, symbol);
		context.getEventQueue().pushEvent((Player)symbol);
	}

}
