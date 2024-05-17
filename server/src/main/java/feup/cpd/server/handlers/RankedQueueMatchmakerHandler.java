package feup.cpd.server.handlers;

import feup.cpd.protocol.models.enums.QueueType;
import feup.cpd.server.repositories.RankedQueueRepository;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class RankedQueueMatchmakerHandler implements Callable<Void> {

    final ExecutorService executorService;

    final int numPlayers;

    static final int MATCHMAKER_TIMEOUT_SECONDS = 5;



    public RankedQueueMatchmakerHandler(ExecutorService executorService, int numPlayers) {
        this.executorService = executorService;
        this.numPlayers = numPlayers;
    }


    @Override
    public Void call() throws Exception {
        Thread.sleep(MATCHMAKER_TIMEOUT_SECONDS*1000);

        var repo = RankedQueueRepository.getInstance(executorService);

        var foundGames = repo.getGameCandidates(numPlayers);

        if(!foundGames.isEmpty()) System.out.printf("Running ranked matchmaker: found %d ranked games.\n", foundGames.size());

        foundGames.forEach((match) -> executorService.submit(new GameFoundHandler(match, QueueType.RANKED)));


        executorService.submit(new RankedQueueMatchmakerHandler(executorService, numPlayers));
        return null;
    }
}
