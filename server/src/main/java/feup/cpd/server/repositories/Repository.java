package feup.cpd.server.repositories;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public abstract class Repository {

    protected final ExecutorService executorService;

    protected Repository(ExecutorService executorService) {
        this.executorService = executorService;
    }

    protected Future<Void> saveAsync(){
        return executorService.submit(new SaveQueueJob());
    }

    protected class SaveQueueJob implements Callable<Void> {
        @Override
        public Void call() throws Exception {
            save();
            return null;
        }
    }

    abstract void save();

    abstract void read();
}
