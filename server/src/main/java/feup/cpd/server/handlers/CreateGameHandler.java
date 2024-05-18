package feup.cpd.server.handlers;

import feup.cpd.game.Game;
import feup.cpd.protocol.models.GameState;
import feup.cpd.server.App;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

public class CreateGameHandler implements Callable<Void> {

    final List<String> playerNames;
    final UUID matchID;

    public CreateGameHandler(List<String> playerNames, UUID matchID) {
        this.playerNames = playerNames;
        this.matchID = matchID;
    }

    @Override
    public Void call() throws Exception {
        Collections.shuffle(playerNames);

        Game newGame = new Game();
        newGame.startGameHeadless(playerNames);

        App.activeGames.put(matchID, newGame);

        var startingPlayer = newGame.getCurrentPlayerName();

        for(var player : playerNames){
            var connection = App.playersLoggedOn.lockAndRead((map) -> map.getInverse(player));

            //no drawn cards considered at the beginning
            var gameState = new GameState(startingPlayer.equals(player), matchID, newGame);

            connection.writeLock.lock();
            try{
                connection.socketChannel.write(gameState.toProtocol());
            } finally {
                connection.writeLock.unlock();
            }
        }
        return null;
    }
}
