package feup.cpd.server.handlers;

import feup.cpd.server.repositories.RankedQueueRepository;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class RankedQueuePropagateHandler implements Callable<Void> {
    private final ExecutorService executorService;

    private static final int PROPAGATE_TIMEOUT = 30;

    public RankedQueuePropagateHandler(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public Void call() throws Exception {
        Thread.sleep(PROPAGATE_TIMEOUT*1000);

        System.out.println("Propagating ranked queue buckets...");

        var repo = RankedQueueRepository.getInstance(executorService);
        repo.propagateBuckets(PROPAGATE_TIMEOUT);

        executorService.submit(new RankedQueuePropagateHandler(executorService));
        return null;
    }
}
