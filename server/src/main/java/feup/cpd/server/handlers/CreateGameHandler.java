package feup.cpd.server.handlers;

import feup.cpd.game.Card;
import feup.cpd.game.Game;
import feup.cpd.protocol.ProtocolFacade;
import feup.cpd.protocol.models.GameState;
import feup.cpd.server.App;
import feup.cpd.server.models.PlayerState;

import java.util.*;
import java.util.concurrent.Callable;

public class CreateGameHandler implements Callable<Void> {

    final List<String> playerNames;
    final UUID matchID;

    public CreateGameHandler(List<String> playerNames, UUID matchID) {
        this.playerNames = new ArrayList<>(playerNames);
        this.matchID = matchID;
    }

    @Override
    public Void call() throws Exception {
        Collections.sort(playerNames);

        Game newGame = new Game();
        newGame.startGameHeadless(playerNames);

        App.activeGames.put(matchID, newGame);

        var startingPlayer = newGame.getCurrentPlayerName();
        List<Card> drawnCards = new ArrayList<>();
        while (true){
            Optional<Card> optionalCard = newGame.checkAndDrawHeadless();
            if(optionalCard.isEmpty()) break;
            drawnCards.add(optionalCard.get());
        }

        for(var player : playerNames){
            var connection = App.playersLoggedOn.lockAndRead((map) -> map.getInverse(player));

            var playerState = App.connectedPlayersState.get(connection);

            playerState.reentrantLock.lock();
            try {
                playerState.value = PlayerState.IN_GAME;
            } finally {
                playerState.reentrantLock.unlock();
            }

            var gameState = new GameState(startingPlayer.equals(player), matchID, newGame.getTopCard(),
                    startingPlayer.equals(player) ? newGame.getCurrentPlayerHand() : new ArrayList<>(),
                    startingPlayer.equals(player) ? drawnCards : new ArrayList<>()
                    );
            connection.writeLock.lock();
            try{
                connection.socketChannel.write(ProtocolFacade.createPacket(gameState));
            } finally {
                connection.writeLock.unlock();
            }
        }
        return null;
    }
}
