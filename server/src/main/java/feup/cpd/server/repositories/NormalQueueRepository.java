package feup.cpd.server.repositories;

import feup.cpd.server.collections.BidirectionalUniqueMap;
import feup.cpd.server.concurrent.ConcurrentRWMap;
import feup.cpd.server.concurrent.helper.RWLockedValue;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class NormalQueueRepository extends Repository {

    private static NormalQueueRepository instance = null;

    private static final String NORMAL_QUEUE_REPOSITORY_PATH = "normalQueue.ser";


    public static NormalQueueRepository getInstance(ExecutorService executorService){
        if(instance == null){
            instance = new NormalQueueRepository(executorService);
        }
        return instance;
    }

    private NormalQueueRepository(ExecutorService executorService) {
        super(executorService);
        this.playersInQueue = new RWLockedValue<>(
                new BidirectionalUniqueMap<>(true),
                new ReentrantReadWriteLock(true));
        this.ioLock = new ReentrantLock();
        read();
    }
    RWLockedValue<BidirectionalUniqueMap<String, UUID>> playersInQueue;

    final ReentrantLock ioLock;

    void save(){
        ioLock.lock();
        try {
            var tempQueue = playersInQueue.lockAndRead(BidirectionalUniqueMap::getAll)
                    .stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            FileOutputStream fileOutputStream = new FileOutputStream(NORMAL_QUEUE_REPOSITORY_PATH);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(tempQueue);
            objectOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            ioLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    void read(){
        ioLock.lock();
        try {
            if(!(new File(NORMAL_QUEUE_REPOSITORY_PATH).exists())){
                return;
            }

            FileInputStream fileInputStream = new FileInputStream(NORMAL_QUEUE_REPOSITORY_PATH);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            //trust me bro check
            Map<String, UUID> readQueue = (Map<String, UUID>) objectInputStream.readObject();
            playersInQueue = new RWLockedValue<>(
                    new BidirectionalUniqueMap<>(readQueue, true),
                    new ReentrantReadWriteLock(true));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            ioLock.unlock();
        }
    }



    public UUID addToQueue(String name){
        var token = UUID.randomUUID();
        playersInQueue.lockAndWrite((val) -> {
                val.put(name, token);
                return null;
        });
        saveAsync();
        return token;
    }

    public void removeUserFromQueue(String name){
        playersInQueue.lockAndWrite((val) -> {
            val.remove(name);
            return null;
        });
    }

    public boolean checkIfUserInQueue(String name){
        return  playersInQueue.lockAndRead((val) ->
            val.containsKey(name));
    }

    public void reAddGameCandidates(Map<String, UUID> gameCandidates){
        playersInQueue.lockAndWrite((val) -> {
            val.addAll(gameCandidates);
            return null;
        });
        saveAsync();
    }

    public boolean checkIfGameCanBeStarted(int numPlayers){
        return playersInQueue.lockAndRead(BidirectionalUniqueMap::size) >= numPlayers;
    }

    public Map<String, UUID> getGameCandidates(int numPlayers){
        var gameCandidates = playersInQueue.lockAndWrite((val) -> val.removeCount(numPlayers));
        saveAsync();
        return  gameCandidates;
    }

}
